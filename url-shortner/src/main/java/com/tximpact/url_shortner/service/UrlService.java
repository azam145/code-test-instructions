package com.tximpact.url_shortner.service;

import com.tximpact.url_shortner.model.UrlEntity;
import com.tximpact.url_shortner.repository.UrlRepository;
import com.tximpact.url_shortner.util.AliasGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private static final int MAX_RETRIES = 5;

    @Transactional
    public UrlEntity createShortUrl(String originalUrl, String customAlias) {

        log.info("Request to shorten URL: {} with custom alias: {}", originalUrl, customAlias);

        String alias;
        if (customAlias != null && !customAlias.isBlank()) {
            // Case 1: Custom Alias provided by user
            if (urlRepository.existsByShortAlias(customAlias)) {
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is already taken.");
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
                .orElseThrow(() -> new RuntimeException("URL with alias '" + alias + "' not found."));
    }

    private String generateUniqueRandomAlias() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String alias = AliasGenerator.generate(7);
            if (!urlRepository.existsByShortAlias(alias)) {
                return alias;
            }
            log.warn("Collision detected for alias: {}. Retry count: {}", alias, i + 1);
        }
        throw new IllegalStateException("Failed to generate a unique alias after " + MAX_RETRIES + " attempts.");
    }
}