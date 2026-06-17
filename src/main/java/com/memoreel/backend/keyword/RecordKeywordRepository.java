package com.memoreel.backend.keyword;

import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.RecordKeyword;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordKeywordRepository extends JpaRepository<RecordKeyword, Long> {

  /**
   * 키워드별 본인 저장 곡 수 (distinct 곡 기준, 명세 §5-1 track_count). 저장이 한 건도 없는 키워드는 결과에 포함되지 않는다(서비스에서 0으로
   * 채움).
   */
  @Query(
      """
            SELECT rk.keyword.id AS keywordId, COUNT(DISTINCT r.song.id) AS trackCount
            FROM RecordKeyword rk
            JOIN rk.record r
            WHERE r.user.id = :userId
            GROUP BY rk.keyword.id
            """)
  List<KeywordTrackCount> countTracksByKeyword(@Param("userId") Long userId);

  /**
   * 특정 키워드로 분류된 본인 저장 곡들의 "가장 최근 저장 record id" 목록 (명세 §5-2). distinct 곡 기준이라 곡별 최신 record(MAX id)만
   * 골라 최근순 정렬한다. 커서(record id)보다 작은 것만 가져와 커서 페이지네이션을 구현한다.
   */
  @Query(
      """
            SELECT MAX(r.id)
            FROM RecordKeyword rk
            JOIN rk.record r
            WHERE r.user.id = :userId AND rk.keyword.id = :keywordId
            GROUP BY r.song.id
            HAVING (:cursor IS NULL OR MAX(r.id) < :cursor)
            ORDER BY MAX(r.id) DESC
            """)
  List<Long> findRecentRecordIds(
      @Param("userId") Long userId,
      @Param("keywordId") Long keywordId,
      @Param("cursor") Long cursor,
      Pageable pageable);

  /** record id 목록으로 곡 정보까지 한 번에 로드한다(N+1 방지). */
  @Query("SELECT r FROM MemoRecord r JOIN FETCH r.song WHERE r.id IN :ids")
  List<MemoRecord> findRecordsWithSong(@Param("ids") List<Long> ids);
}
