package com.tpximpact.urlshortener.service;


import com.tpximpact.urlshortener.TestcontainersConfiguration;
import com.tpximpact.urlshortener.model.ErrorResponse;
import com.tpximpact.urlshortener.model.UrlEntity;
import com.tpximpact.urlshortener.repository.UrlRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class) // Spins up the real Postgres Docker container
@Transactional // Rolls back the database after every test
class UrlServiceIT {

    @Autowired
    private UrlService urlService;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    @DisplayName("Full Flow: Create, Persist, and Retrieve")
    void testFullShorteningFlow() throws ErrorResponse {
        // 1. Create
        String longUrl = "https://www.tpximpact.com/digital-transformation";
        UrlEntity saved = urlService.createShortUrl(longUrl, null);

        // 2. Verify Persistence
        assertThat(urlRepository.existsByShortAlias(saved.getShortAlias())).isTrue();

        // 3. Retrieve
        UrlEntity retrieved = urlService.getOriginalUrl(saved.getShortAlias());
        assertThat(retrieved.getOriginalUrl()).isEqualTo(longUrl);
    }

    @Test
    @DisplayName("Should prevent duplicate custom aliases")
    void shouldPreventDuplicateCustomAliases() throws ErrorResponse {
        String custom = "my-link";
        urlService.createShortUrl("https://site1.com", custom);

        // Attempting to use the same alias again should throw an exception
        assertThatThrownBy(() -> urlService.createShortUrl("https://site2.com", custom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("Should remove record from DB on delete")
    void shouldRemoveRecordOnDelete() throws ErrorResponse {
        UrlEntity saved = urlService.createShortUrl("https://temp.com", "temp");
        urlService.deleteUrl("temp");

        assertThat(urlRepository.existsByShortAlias("temp")).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent alias")
    void shouldThrowExceptionOnNonExistentDelete() {
        AssertionsForClassTypes.assertThatThrownBy(() -> urlService.deleteUrl("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

}