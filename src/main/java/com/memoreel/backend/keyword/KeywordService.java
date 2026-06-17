package com.memoreel.backend.keyword;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.common.pagination.CursorCodec;
import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.User;
import com.memoreel.backend.keyword.dto.KeywordRef;
import com.memoreel.backend.keyword.dto.KeywordSummaryResponse;
import com.memoreel.backend.keyword.dto.KeywordTracksData;
import com.memoreel.backend.keyword.dto.SavedTrackResponse;
import com.memoreel.backend.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeywordService {

  private final KeywordRepository keywordRepository;
  private final RecordKeywordRepository recordKeywordRepository;
  private final UserService userService;

  public KeywordService(
      KeywordRepository keywordRepository,
      RecordKeywordRepository recordKeywordRepository,
      UserService userService) {
    this.keywordRepository = keywordRepository;
    this.recordKeywordRepository = recordKeywordRepository;
    this.userService = userService;
  }

  /** 마스터 키워드 전체 + 본인 저장 곡 수 (명세 §5-1). */
  @Transactional(readOnly = true)
  public List<KeywordSummaryResponse> getKeywords(String deviceId) {
    User user = userService.getByDeviceId(deviceId);

    Map<Long, Long> countByKeywordId =
        recordKeywordRepository.countTracksByKeyword(user.getId()).stream()
            .collect(
                Collectors.toMap(
                    KeywordTrackCount::getKeywordId, KeywordTrackCount::getTrackCount));

    return keywordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
        .map(
            keyword ->
                new KeywordSummaryResponse(
                    keyword.getId(),
                    keyword.getName(),
                    countByKeywordId.getOrDefault(keyword.getId(), 0L)))
        .toList();
  }

  /** 특정 키워드로 분류된 본인 저장 곡 목록 (명세 §5-2). distinct 곡, 최근 저장순, 커서 페이지네이션. */
  @Transactional(readOnly = true)
  public KeywordTracksData getTracks(String deviceId, Long keywordId, String cursor, int limit) {
    User user = userService.getByDeviceId(deviceId);
    Keyword keyword =
        keywordRepository
            .findById(keywordId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

    Long cursorRecordId = (cursor == null) ? null : Long.valueOf(CursorCodec.decode(cursor));

    // limit + 1로 다음 페이지 존재 여부 판단
    List<Long> recordIds =
        recordKeywordRepository.findRecentRecordIds(
            user.getId(), keywordId, cursorRecordId, PageRequest.of(0, limit + 1));

    boolean hasNext = recordIds.size() > limit;
    List<Long> pageIds = hasNext ? recordIds.subList(0, limit) : recordIds;

    Map<Long, MemoRecord> recordById =
        pageIds.isEmpty()
            ? Map.of()
            : recordKeywordRepository.findRecordsWithSong(pageIds).stream()
                .collect(Collectors.toMap(MemoRecord::getId, Function.identity()));

    List<SavedTrackResponse> items =
        pageIds.stream().map(recordById::get).map(SavedTrackResponse::from).toList();

    String nextCursor =
        hasNext ? CursorCodec.encode(String.valueOf(pageIds.get(pageIds.size() - 1))) : null;

    return new KeywordTracksData(KeywordRef.from(keyword), items, nextCursor);
  }
}
