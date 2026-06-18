package com.memoreel.backend.record;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.RecordKeyword;
import com.memoreel.backend.entity.Song;
import com.memoreel.backend.entity.User;
import com.memoreel.backend.keyword.KeywordRepository;
import com.memoreel.backend.keyword.RecordKeywordRepository;
import com.memoreel.backend.record.dto.RecordCreateRequest;
import com.memoreel.backend.record.dto.RecordResponse;
import com.memoreel.backend.song.SongRepository;
import com.memoreel.backend.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 좋아요한 1곡과 사진을 기록으로 저장한다 (명세 §4-1). */
@Service
@Transactional
public class RecordService {

  private final UserRepository userRepository;
  private final SongRepository songRepository;
  private final KeywordRepository keywordRepository;
  private final MemoRecordRepository memoRecordRepository;
  private final RecordKeywordRepository recordKeywordRepository;

  public RecordService(
      UserRepository userRepository,
      SongRepository songRepository,
      KeywordRepository keywordRepository,
      MemoRecordRepository memoRecordRepository,
      RecordKeywordRepository recordKeywordRepository) {
    this.userRepository = userRepository;
    this.songRepository = songRepository;
    this.keywordRepository = keywordRepository;
    this.memoRecordRepository = memoRecordRepository;
    this.recordKeywordRepository = recordKeywordRepository;
  }

  public RecordResponse create(String deviceId, RecordCreateRequest request) {
    User user =
        userRepository
            .findByDeviceId(deviceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    List<Keyword> keywords = loadKeywords(request.keywordIds());
    Song song = upsertSong(request.track());
    MemoRecord record = saveRecord(user, song, request);
    keywords.forEach(
        keyword ->
            recordKeywordRepository.save(
                RecordKeyword.builder().record(record).keyword(keyword).build()));

    return RecordResponse.of(record, keywords);
  }

  private List<Keyword> loadKeywords(List<Long> keywordIds) {
    List<Keyword> found = keywordRepository.findAllById(keywordIds);
    if (found.size() != keywordIds.size()) {
      throw new BusinessException(
          ErrorCode.VALIDATION_ERROR, "존재하지 않는 keyword_id가 포함되어 있습니다.", null);
    }
    return found;
  }

  private Song upsertSong(RecordCreateRequest.Track track) {
    return songRepository
        .findByItunesTrackId(track.trackId())
        .map(
            existing -> {
              if (memoRecordRepository.existsBySong(existing)) {
                throw new BusinessException(
                    ErrorCode.CONFLICT, "이미 동일한 곡으로 저장된 record가 있습니다.", null);
              }
              return existing;
            })
        .orElseGet(
            () ->
                songRepository.save(
                    Song.builder()
                        .itunesTrackId(track.trackId())
                        .trackName(track.title())
                        .artistName(track.artist())
                        .artworkUrl(track.artworkUrl())
                        .previewUrl(track.previewUrl())
                        .build()));
  }

  private MemoRecord saveRecord(User user, Song song, RecordCreateRequest request) {
    RecordCreateRequest.Location location = request.location();
    BigDecimal lat = location == null ? null : location.lat();
    BigDecimal lng = location == null ? null : location.lng();
    String label = location == null ? null : location.label();
    return memoRecordRepository.save(
        MemoRecord.builder()
            .user(user)
            .song(song)
            .photoUrl(request.photoUrl())
            .locationLat(lat)
            .locationLng(lng)
            .locationLabel(label)
            .build());
  }
}
