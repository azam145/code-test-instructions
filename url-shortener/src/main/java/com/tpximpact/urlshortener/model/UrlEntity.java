package com.tpximpact.urlshortener.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "urls")
@Getter
@Setter
@Builder                       // Generates builder()
@NoArgsConstructor            // Required by Hibernate/JPA
@AllArgsConstructor           // Required by @Builder
public class UrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048)
    @Pattern(regexp = "^https?://.*", message = "Must be a valid HTTP or HTTPS URL")
    private String originalUrl;

    @Column(unique = true)
    private String shortAlias;

    private OffsetDateTime createdAt;
}