package com.tpximpact.urlshortener.controller;

import com.tpximpact.urlshortener.model.ErrorResponse;
import com.tpximpact.urlshortener.model.UrlEntity;
import com.tpximpact.urlshortener.service.UrlService;
import com.tpximpact.urlshortener.model.ShortenPostRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // Only from .web.servlet.result

@WebMvcTest(UrlController.class) // Only loads the web layer
class UrlControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    @DisplayName("GET /{alias} should redirect to original URL with 302 Found")
    void shouldRedirectToOriginalUrl() throws Exception {
        // Given
        String alias = "tpx123";
        String originalUrl = "https://www.tpximpact.com";
        UrlEntity mockEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortAlias(alias)
                .build();

        when(urlService.getOriginalUrl(alias)).thenReturn(mockEntity);

        // When & Then
        mockMvc.perform(get("/{alias}", alias))
                .andExpect(status().isFound()) // Assert 302 Found
                .andExpect(header().string("Location", originalUrl));
    }

    @Test
    @DisplayName("POST /shorten should return 201 Created and the shortened URL")
    void shouldCreateShortUrl() throws Exception, ErrorResponse {
        // Given
        ShortenPostRequest request = new ShortenPostRequest();
        request.setFullUrl("https://google.com");

        UrlEntity mockEntity = UrlEntity.builder()
                .shortAlias("goog123")
                .originalUrl("https://google.com")
                .build();

        when(urlService.createShortUrl(any(String.class), any())).thenReturn(mockEntity);

        // When & Then
        mockMvc.perform(post("/shorten")  // Changed from /urls to /shorten
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/goog123")); // Also changed to match response field
    }

}