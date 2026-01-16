package com.tpximpact.urlshortener.repository;

import com.tpximpact.urlshortener.TestcontainersConfiguration;
import com.tpximpact.urlshortener.model.UrlEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use the real Postgres container
class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    @Test
    @DisplayName("Should find by short URL")
    void shouldFindByShortAlias() {
        // Given
        UrlEntity entity = UrlEntity.builder()
                .originalUrl("https://example.com")
                .shortAlias("abc123")
                .createdAt(OffsetDateTime.now())
                .build();
        urlRepository.save(entity);

        // When
        Optional<UrlEntity> found = urlRepository.findByShortAlias("abc123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("Should Return Empty When Alias Does Not Exist")
    void shouldReturnEmptyWhenAliasDoesNotExist() {
        Optional<UrlEntity> found = urlRepository.findByShortAlias("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete an existing URL")
    void shouldDeleteExistingUrl() {
        // Given
        String alias = "del123";
        UrlEntity entity = UrlEntity.builder()
                .originalUrl("https://example.com")
                .shortAlias(alias)
                .createdAt(OffsetDateTime.now())
                .build();
        urlRepository.save(entity);

        // When
        urlRepository.deleteByShortAlias(alias);

        // Then
        assertThat(urlRepository.existsByShortAlias(alias)).isFalse();
    }
}