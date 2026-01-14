package com.tximpact.url_shortner.service;

import com.tximpact.url_shortner.TestcontainersConfiguration;
import com.tximpact.url_shortner.model.UrlEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class) // Uses your real Postgres container
@Transactional // Rolls back DB changes after each test
class UrlServiceTest {

    @Autowired
    private UrlService urlService;

    @Test
    void shouldSaveAndRetrieveUrl() {
        // Given
        String originalUrl = "https://www.tpximpact.com/careers";

        // When
        UrlEntity saved = urlService.createShortUrl(originalUrl, null);

        // Then
        assertThat(saved.getShortAlias()).isNotNull().hasSize(7);
        assertThat(saved.getOriginalUrl()).isEqualTo(originalUrl);

        UrlEntity retrieved = urlService.getOriginalUrl(saved.getShortAlias());
        assertThat(retrieved.getOriginalUrl()).isEqualTo(originalUrl);
    }

    @Test
    void shouldAllowCustomAlias() {
        String custom = "my-custom-link";
        UrlEntity saved = urlService.createShortUrl("https://google.com", custom);

        assertThat(saved.getShortAlias()).isEqualTo(custom);
    }
}