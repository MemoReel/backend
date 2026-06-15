package com.memoreel.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "exif_lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "exif_lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public Photo(String url, Integer width, Integer height, LocalDateTime takenAt,
                 BigDecimal lat, BigDecimal lng, LocalDateTime expiresAt) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.takenAt = takenAt;
        this.lat = lat;
        this.lng = lng;
        this.expiresAt = expiresAt;
    }
}
