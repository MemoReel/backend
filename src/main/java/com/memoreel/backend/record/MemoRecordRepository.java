package com.memoreel.backend.record;

import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.Song;
import com.memoreel.backend.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemoRecordRepository extends JpaRepository<MemoRecord, Long> {

  boolean existsBySong(Song song);

  /** 본인 record 최신순 + song 동시 fetch (N+1 회피). */
  @Query(
      """
        SELECT r FROM MemoRecord r
        JOIN FETCH r.song
        WHERE r.user = :user
        ORDER BY r.createdAt DESC
        """)
  List<MemoRecord> findAllByUserWithSongOrderByCreatedAtDesc(@Param("user") User user);
}
