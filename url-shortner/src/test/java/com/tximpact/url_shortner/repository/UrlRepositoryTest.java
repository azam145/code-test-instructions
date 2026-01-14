package com.tximpact.url_shortner.repository;

import com.tximpact.url_shortner.TestcontainersConfiguration;
import com.tximpact.url_shortner.model.UrlEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use the real Postgres container
class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    @Test
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
    void shouldReturnEmptyWhenAliasDoesNotExist() {
        Optional<UrlEntity> found = urlRepository.findByShortAlias("nonexistent");
        assertThat(found).isEmpty();
    }
}