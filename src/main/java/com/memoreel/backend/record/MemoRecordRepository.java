package com.memoreel.backend.record;

import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRecordRepository extends JpaRepository<MemoRecord, Long> {

  boolean existsBySong(Song song);
}
