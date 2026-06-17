package com.memoreel.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoRecord extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "song_id", nullable = false)
  private Song song;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "photo_id", nullable = false)
  private Photo photo;

  @Column(name = "location_lat", precision = 10, scale = 7)
  private BigDecimal locationLat;

  @Column(name = "location_lng", precision = 10, scale = 7)
  private BigDecimal locationLng;

  @Column(name = "location_label")
  private String locationLabel;

  @Builder
  public MemoRecord(
      User user,
      Song song,
      Photo photo,
      BigDecimal locationLat,
      BigDecimal locationLng,
      String locationLabel) {
    this.user = user;
    this.song = song;
    this.photo = photo;
    this.locationLat = locationLat;
    this.locationLng = locationLng;
    this.locationLabel = locationLabel;
  }
}
