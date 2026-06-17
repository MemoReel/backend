package com.memoreel.backend.keyword;

import com.memoreel.backend.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {}
