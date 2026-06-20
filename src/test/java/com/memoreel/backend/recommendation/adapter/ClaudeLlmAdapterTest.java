package com.memoreel.backend.recommendation.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.services.blocking.MessageService;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.keyword.KeywordRepository;
import com.memoreel.backend.recommendation.adapter.claude.ClaudePromptBuilder;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeProperties;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeResponseParser;
import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.SongCandidate;
import com.memoreel.backend.recommendation.port.StoragePort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ClaudeLlmAdapterTest {

  private AnthropicClient client;
  private MessageService messageService;
  private StoragePort storagePort;
  private KeywordRepository keywordRepository;
  private ClaudeResponseParser responseParser;
  private ClaudeLlmAdapter adapter;

  @BeforeEach
  void setUp() {
    client = mock(AnthropicClient.class);
    messageService = mock(MessageService.class);
    when(client.messages()).thenReturn(messageService);

    storagePort = mock(StoragePort.class);
    keywordRepository = mock(KeywordRepository.class);
    when(keywordRepository.findAll()).thenReturn(List.of(Keyword.builder().name("노을").build()));
    responseParser = mock(ClaudeResponseParser.class);

    adapter =
        new ClaudeLlmAdapter(
            client,
            storagePort,
            keywordRepository,
            new ClaudeProperties("key", "claude-sonnet-4-6", 1024, 30, 2),
            new ClaudePromptBuilder(new ObjectMapper()),
            responseParser);
    adapter.loadKeywordCache();
  }

  @Test
  void analyzePhoto_정상_흐름은_파서_결과를_그대로_반환한다() {
    when(storagePort.viewUrl("temp/abc.jpg")).thenReturn("https://signed/abc.jpg");
    Message message = mock(Message.class);
    when(messageService.create(any(MessageCreateParams.class))).thenReturn(message);
    LlmAnalysis expected =
        new LlmAnalysis(
            "노을", List.of("노을"), List.of(new SongCandidate("Beach", "The Neighbourhood")));
    when(responseParser.parseAnalyzePhoto(message)).thenReturn(expected);

    LlmAnalysis result = adapter.analyzePhoto("temp/abc.jpg");

    assertThat(result).isSameAs(expected);
  }

  @Test
  void analyzePhoto_SDK_예외는_UPSTREAM_ERROR로_매핑한다() {
    when(storagePort.viewUrl("temp/abc.jpg")).thenReturn("https://signed/abc.jpg");
    when(messageService.create(any(MessageCreateParams.class)))
        .thenThrow(new AnthropicException("timeout"));

    assertThatThrownBy(() -> adapter.analyzePhoto("temp/abc.jpg"))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UPSTREAM_ERROR));
  }

  @Test
  void recommendSongs_정상_흐름은_파서_결과를_그대로_반환한다() {
    Message message = mock(Message.class);
    when(messageService.create(any(MessageCreateParams.class))).thenReturn(message);
    List<SongCandidate> expected = List.of(new SongCandidate("Sunflower", "Post Malone"));
    when(responseParser.parseRecommendSongs(message)).thenReturn(expected);

    List<SongCandidate> result =
        adapter.recommendSongs(
            "노을", List.of("노을"), List.of(new SongCandidate("Beach", "The Neighbourhood")));

    assertThat(result).isSameAs(expected);
  }

  @Test
  void recommendSongs_SDK_예외는_UPSTREAM_ERROR로_매핑한다() {
    when(messageService.create(any(MessageCreateParams.class)))
        .thenThrow(new AnthropicException("network"));

    assertThatThrownBy(() -> adapter.recommendSongs("노을", List.of("노을"), List.of()))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UPSTREAM_ERROR));
  }
}
