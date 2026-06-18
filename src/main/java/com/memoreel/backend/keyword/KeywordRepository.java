package com.memoreel.backend.keyword;

import com.memoreel.backend.entity.Keyword;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  List<Keyword> findByNameIn(Collection<String> names);
}
