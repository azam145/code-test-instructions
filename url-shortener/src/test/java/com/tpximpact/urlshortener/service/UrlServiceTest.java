package com.tpximpact.urlshortener.service;

import com.tpximpact.urlshortener.TestcontainersConfiguration;
import com.tpximpact.urlshortener.model.UrlEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class) // Uses your real Postgres container
@Transactional // Rolls back DB changes after each test
class UrlServiceTest {

    @Autowired
    private UrlService urlService;

    @Test
    @DisplayName("Should Save And Retrieve URL")
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
    @DisplayName("Should Allow Custom Alias")
    void shouldAllowCustomAlias() {
        String custom = "my-custom-link";
        UrlEntity saved = urlService.createShortUrl("https://google.com", custom);

        assertThat(saved.getShortAlias()).isEqualTo(custom);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent alias")
    void shouldThrowExceptionOnNonExistentDelete() {
        assertThatThrownBy(() -> urlService.deleteUrl("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

}