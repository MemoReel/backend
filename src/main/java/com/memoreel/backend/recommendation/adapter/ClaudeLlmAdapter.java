package com.memoreel.backend.recommendation.adapter;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.ToolChoice;
import com.anthropic.models.messages.ToolChoiceTool;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.keyword.KeywordRepository;
import com.memoreel.backend.recommendation.adapter.claude.ClaudePromptBuilder;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeProperties;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeResponseParser;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeToolSchemas;
import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.LlmPort;
import com.memoreel.backend.recommendation.port.SongCandidate;
import com.memoreel.backend.recommendation.port.StoragePort;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** {@link LlmPort}의 Claude Messages API 구현. {@code memoreel.llm.provider=claude}일 때만 등록된다. */
@Component
@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "claude")
public class ClaudeLlmAdapter implements LlmPort {

  private static final Logger log = LoggerFactory.getLogger(ClaudeLlmAdapter.class);

  private final AnthropicClient client;
  private final StoragePort storagePort;
  private final KeywordRepository keywordRepository;
  private final ClaudeProperties properties;
  private final ClaudePromptBuilder promptBuilder;
  private final ClaudeResponseParser responseParser;

  private List<String> cachedKeywordNames = List.of();

  public ClaudeLlmAdapter(
      AnthropicClient client,
      StoragePort storagePort,
      KeywordRepository keywordRepository,
      ClaudeProperties properties,
      ClaudePromptBuilder promptBuilder,
      ClaudeResponseParser responseParser) {
    this.client = client;
    this.storagePort = storagePort;
    this.keywordRepository = keywordRepository;
    this.properties = properties;
    this.promptBuilder = promptBuilder;
    this.responseParser = responseParser;
  }

  @PostConstruct
  void loadKeywordCache() {
    cachedKeywordNames = keywordRepository.findAll().stream().map(Keyword::getName).toList();
    log.info("Claude 어댑터 키워드 캐시 로드: count={}", cachedKeywordNames.size());
  }

  @Override
  public LlmAnalysis analyzePhoto(String photoUrl) {
    String imageUrl = storagePort.viewUrl(photoUrl);
    MessageCreateParams params =
        baseParams()
            .system(promptBuilder.analyzeSystemPrompt(cachedKeywordNames))
            .addMessage(promptBuilder.analyzeUserMessage(imageUrl))
            .addTool(ClaudeToolSchemas.analyzePhotoTool())
            .toolChoice(toolChoice(ClaudeToolSchemas.ANALYZE_PHOTO_TOOL_NAME))
            .build();
    Message message = invoke(params, "analyzePhoto");
    return responseParser.parseAnalyzePhoto(message);
  }

  @Override
  public List<SongCandidate> recommendSongs(
      String description, List<String> keywordNames, List<SongCandidate> excludeTracks) {
    MessageCreateParams params =
        baseParams()
            .system(promptBuilder.recommendSystemPrompt())
            .addMessage(
                promptBuilder.recommendUserMessage(description, keywordNames, excludeTracks))
            .addTool(ClaudeToolSchemas.recommendSongsTool())
            .toolChoice(toolChoice(ClaudeToolSchemas.RECOMMEND_SONGS_TOOL_NAME))
            .build();
    Message message = invoke(params, "recommendSongs");
    return responseParser.parseRecommendSongs(message);
  }

  private MessageCreateParams.Builder baseParams() {
    return MessageCreateParams.builder()
        .model(properties.model())
        .maxTokens(properties.maxTokens());
  }

  private ToolChoice toolChoice(String name) {
    return ToolChoice.ofTool(ToolChoiceTool.builder().name(name).build());
  }

  private Message invoke(MessageCreateParams params, String stage) {
    try {
      return client.messages().create(params);
    } catch (AnthropicException e) {
      log.warn("Claude 호출 실패: stage={}, message={}", stage, e.getMessage());
      log.debug("Claude 호출 실패 상세", e);
      throw new BusinessException(ErrorCode.UPSTREAM_ERROR, "LLM 호출 실패", null);
    }
  }
}
