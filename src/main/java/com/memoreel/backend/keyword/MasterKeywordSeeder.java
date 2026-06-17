package com.memoreel.backend.keyword;

import com.memoreel.backend.entity.Keyword;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 부팅 시점에 {@link MasterKeywordCatalog#NAMES}를 DB와 동기화한다.
 *
 * <ul>
 *   <li>코드에 있는데 DB에 없는 이름 → INSERT
 *   <li>DB에 있는데 코드에 없는 이름 → 유지 (record_keywords가 참조 중일 수 있어 함부로 삭제 X)
 * </ul>
 */
@Component
public class MasterKeywordSeeder implements ApplicationRunner {

  private final KeywordRepository keywordRepository;

  public MasterKeywordSeeder(KeywordRepository keywordRepository) {
    this.keywordRepository = keywordRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    Set<String> existing =
        keywordRepository.findAll().stream().map(Keyword::getName).collect(Collectors.toSet());

    MasterKeywordCatalog.NAMES.stream()
        .filter(name -> !existing.contains(name))
        .map(name -> Keyword.builder().name(name).build())
        .forEach(keywordRepository::save);
  }
}
