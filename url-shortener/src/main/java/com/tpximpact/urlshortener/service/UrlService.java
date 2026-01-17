package com.tpximpact.urlshortener.service;

import com.tpximpact.urlshortener.exception.DuplicateAliasException;
import com.tpximpact.urlshortener.exception.UrlNotFoundException;
import com.tpximpact.urlshortener.model.ErrorResponse;
import com.tpximpact.urlshortener.model.UrlEntity;
import com.tpximpact.urlshortener.repository.UrlRepository;
import com.tpximpact.urlshortener.util.AliasGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private static final int MAX_RETRIES = 5;
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final int MIN_ALIAS_LENGTH = 1;
    private static final int MAX_ALIAS_LENGTH = 50;
    private static final int MAX_URL_LENGTH = 2048;

    @Transactional
    public UrlEntity createShortUrl(String originalUrl, String customAlias) throws ErrorResponse {

        log.info("Request to shorten URL: {} with custom alias: {}", originalUrl, customAlias);
        validateUrl(originalUrl);
        String alias;
        if (customAlias != null && !customAlias.isBlank()) {
            // Case 1: Custom Alias provided by user
            if (urlRepository.existsByShortAlias(customAlias)) {
                throw new DuplicateAliasException(customAlias);
            }
            alias = customAlias;
        } else {
            // Case 2: Generate random alias with collision retry logic
            alias = generateUniqueRandomAlias();
        }

        UrlEntity entity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortAlias(alias)
                .createdAt(OffsetDateTime.now())
                .build();

        return urlRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public UrlEntity getOriginalUrl(String alias) {
        return urlRepository.findByShortAlias(alias)
                .orElseThrow(() -> new UrlNotFoundException(alias));
    }

    @Transactional // Required for delete operations
    public void deleteUrl(String alias) {
        log.info("Attempting to delete URL with alias: {}", alias);
        // 1. Check if it exists first
        if (!urlRepository.existsByShortAlias(alias)) {
            log.error("Delete failed: Alias {} not found", alias);
            // This will be caught by your GlobalExceptionHandler and turned into a 404
            throw new UrlNotFoundException(alias);
        }

        // 2. Perform the delete
        urlRepository.deleteByShortAlias(alias);
        log.info("Successfully deleted alias: {}", alias);
    }

    private String generateUniqueRandomAlias() throws ErrorResponse {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String alias = AliasGenerator.generate(7);
            if (!urlRepository.existsByShortAlias(alias)) {
                return alias;
            }
            log.warn("Collision detected for alias: {}. Retry count: {}", alias, i + 1);
        }
        throw new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Failed to generate a unique alias after " + MAX_RETRIES + " attempts.");
    }

    private void validateUrl(String url) throws ErrorResponse {
        if (url == null || url.trim().isEmpty()) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "URL cannot be null or empty");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw new ErrorResponse(HttpStatus.URI_TOO_LONG.value(),"URL cannot exceed " + MAX_URL_LENGTH + " characters");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Must be a valid URL format");
        }
    }

}