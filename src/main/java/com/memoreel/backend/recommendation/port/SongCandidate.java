package com.memoreel.backend.recommendation.port;

/** LLM이 제안하는 곡 후보 (제목+아티스트). iTunes Search로 실제 트랙 매칭 전 단계의 원시 텍스트. */
public record SongCandidate(String title, String artist) {}
