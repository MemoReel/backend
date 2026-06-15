package com.memoreel.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Song extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "itunes_track_id", nullable = false, length = 32, unique = true)
    private String itunesTrackId;

    @Column(name = "track_name", nullable = false)
    private String trackName;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "artwork_url", length = 512)
    private String artworkUrl;

    @Column(name = "preview_url", length = 512)
    private String previewUrl;

    @Builder
    public Song(String itunesTrackId, String trackName, String artistName,
                String artworkUrl, String previewUrl) {
        this.itunesTrackId = itunesTrackId;
        this.trackName = trackName;
        this.artistName = artistName;
        this.artworkUrl = artworkUrl;
        this.previewUrl = previewUrl;
    }
}
