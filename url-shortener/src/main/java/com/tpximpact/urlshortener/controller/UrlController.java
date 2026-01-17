package com.tpximpact.urlshortener.controller;

import com.tpximpact.urlshortener.api.AliasApi;
import com.tpximpact.urlshortener.api.ShortenApi;
import com.tpximpact.urlshortener.api.UrlsApi;
import com.tpximpact.urlshortener.model.*;
import com.tpximpact.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.net.URI;
import java.util.Optional;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController implements UrlsApi, ShortenApi, AliasApi {

    private final UrlService urlService;

    @Value("${app.base-url:http://localhost:8080/}")
    private String baseUrl;

    // Implement the aliasGet method from AliasApi instead of creating a duplicate mapping
    @Override
    public ResponseEntity<Void> aliasGet(String alias) {
        var entity = urlService.getOriginalUrl(alias);
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Found
                .location(URI.create(entity.getOriginalUrl()))
                .build();
    }

    @Override
    public ResponseEntity<ShortenPost201Response> shortenPost(ShortenPostRequest shortenPostRequest) {
        // Delegate to our Service
        UrlEntity entity = null;
        try {
            entity = urlService.createShortUrl(
                    shortenPostRequest.getFullUrl(),
                    shortenPostRequest.getCustomAlias()
            );
        } catch (ErrorResponse e) {
            throw new RuntimeException(e);
        }

        // Build the Response DTO
        ShortenPost201Response response = new ShortenPost201Response();
        response.setShortUrl(baseUrl + entity.getShortAlias());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return UrlsApi.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> aliasDelete(String alias) {
        urlService.deleteUrl(alias); // You'll need to add this method to your Service
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @Override
    public ResponseEntity<List<UrlsGet200ResponseInner>> urlsGet() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}