package com.tximpact.url_shortner.model;

import jakarta.persistence.*;
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

    private String originalUrl;

    @Column(unique = true)
    private String shortAlias;

    private OffsetDateTime createdAt;
}