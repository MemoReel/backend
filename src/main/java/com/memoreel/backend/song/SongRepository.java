package com.memoreel.backend.song;

import com.memoreel.backend.entity.Song;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

  Optional<Song> findByItunesTrackId(String itunesTrackId);
}
