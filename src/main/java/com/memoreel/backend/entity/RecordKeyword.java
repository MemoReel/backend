package com.memoreel.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "record_keywords",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_record_keywords",
            columnNames = {"record_id", "keyword_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordKeyword extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "record_id", nullable = false)
  private MemoRecord record;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "keyword_id", nullable = false)
  private Keyword keyword;

  @Builder
  public RecordKeyword(MemoRecord record, Keyword keyword) {
    this.record = record;
    this.keyword = keyword;
  }
}
