package com.tpximpact.urlshortener.repository;

import com.tpximpact.urlshortener.model.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    // For redirection: This is the most important query
    Optional<UrlEntity> findByShortAlias(String shortAlias);

    // To prevent duplicate shortening of the same URL (Optional but good)
    Optional<UrlEntity> findByOriginalUrl(String originalUrl);

    boolean existsByShortAlias(String shortAlias);
    // Derived delete query
    void deleteByShortAlias(String shortAlias);

}