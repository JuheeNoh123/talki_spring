package springkong.talki_spring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springkong.talki_spring.domain.PracticeSession;
import springkong.talki_spring.domain.PracticeSubStepResult;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.PracticeSessionStateDTO;
import springkong.talki_spring.dto.request.PracticeRequestDTO;
import springkong.talki_spring.dto.response.PracticeResponseDTO;
import springkong.talki_spring.enums.PracticeMode;
import springkong.talki_spring.enums.PracticeStep;
import springkong.talki_spring.enums.PracticeType;
import springkong.talki_spring.exception.NotFoundException;
import springkong.talki_spring.repository.PracticeSessionRepository;
import springkong.talki_spring.repository.PracticeSubStepResultRepository;
import springkong.talki_spring.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private static final String KEY_PREFIX = "practice:session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final PracticeSubStepResultRepository practiceSubStepResultRepository;

    // ===== 0단계: 훈련 시작 =====

    public PracticeResponseDTO.SessionStartResponse startSession(User user) {
        String sessionId = UUID.randomUUID().toString();

        PracticeSessionStateDTO state = new PracticeSessionStateDTO();
        state.setUserId(user.getId());
        state.setCurrentStep(PracticeStep.THOUGHT_RECOGNITION);
        saveState(sessionId, state);

        PracticeResponseDTO.SessionStartResponse response = new PracticeResponseDTO.SessionStartResponse();
        response.setSessionId(sessionId);
        response.setCurrentStep(state.getCurrentStep());
        return response;
    }

    // ===== 1단계: 자동사고 인식 =====

    public PracticeResponseDTO.StepResponse saveThoughtRecognition(
            String sessionId, Long userId, PracticeRequestDTO.ThoughtRecognitionRequest request) {

        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);
        state.setSelectedThoughts(request.getSelectedThoughts());
        state.setCustomThought(request.getCustomThought());
        state.setCurrentStep(PracticeStep.BREATHING);
        saveState(sessionId, state);

        return toStepResponse(state);
    }

    // ===== 2단계: 호흡 조절 =====

    public PracticeResponseDTO.StepResponse saveBreathing(
            String sessionId, Long userId, PracticeRequestDTO.BreathingRequest request) {

        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);
        state.setBreathingCompleted(request.isCompleted());
        state.setCurrentStep(PracticeStep.MODE_SELECTION);
        saveState(sessionId, state);

        return toStepResponse(state);
    }

    // ===== 3단계: 연습 모드 선택 =====

    public PracticeResponseDTO.StepResponse selectMode(
            String sessionId, Long userId, PracticeRequestDTO.ModeRequest request) {

        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);
        PracticeMode mode = request.getMode();
        state.setMode(mode);

        if (mode == PracticeMode.SCRIPT_BASED) {
            state.setSubStepQueue(List.of(PracticeType.SCRIPT, PracticeType.GAZE));
            state.setCurrentStep(PracticeStep.PRACTICE_IN_PROGRESS);
        } else if (mode == PracticeMode.IMPROMPTU) {
            state.setSubStepQueue(List.of(PracticeType.IMPROMPTU, PracticeType.KEYWORD, PracticeType.POINT));
            state.setCurrentStep(PracticeStep.PRACTICE_IN_PROGRESS);
        } else {
            state.setSubStepQueue(List.of());
            state.setCurrentStep(PracticeStep.BEHAVIORAL_EXPERIMENT);
        }
        saveState(sessionId, state);

        return toStepResponse(state);
    }

    // ===== 4단계: 연습 진행 (WebSocket 핸들러에서 사용) =====

    public boolean isSubStepPending(String sessionId, String subStepRaw) {
        PracticeSessionStateDTO state = loadStateOrNull(sessionId);
        if (state == null || state.getCurrentStep() != PracticeStep.PRACTICE_IN_PROGRESS) {
            return false;
        }
        PracticeType subStep = parseSubStep(subStepRaw);
        if (subStep == null) return false;

        return state.getSubStepQueue().contains(subStep)
                && !state.getCompletedSubSteps().contains(subStep);
    }

    public void appendSubStepResult(String sessionId, String subStepRaw, JsonNode resultNode) {
        PracticeSessionStateDTO state = loadStateOrNull(sessionId);
        if (state == null) return;

        PracticeType subStep = parseSubStep(subStepRaw);
        if (subStep == null) return;

        PracticeSessionStateDTO.SubStepResult result = new PracticeSessionStateDTO.SubStepResult();
        result.setSubStep(subStep);
        result.setScore(resultNode.hasNonNull("score") ? resultNode.get("score").asInt() : null);
        result.setFeedbackText(resultNode.hasNonNull("feedback_text") ? resultNode.get("feedback_text").asText() : null);
        result.setRawResultJson(resultNode.has("raw_result") ? resultNode.get("raw_result").toString() : null);

        state.getSubStepResults().add(result);
        if (!state.getCompletedSubSteps().contains(subStep)) {
            state.getCompletedSubSteps().add(subStep);
        }

        if (state.getCompletedSubSteps().containsAll(state.getSubStepQueue())) {
            state.setCurrentStep(PracticeStep.BEHAVIORAL_EXPERIMENT);
        }

        saveState(sessionId, state);
    }

    // ===== 진행 상태 조회 =====

    public PracticeResponseDTO.SessionStateResponse getSessionState(String sessionId, Long userId) {
        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);

        PracticeResponseDTO.SessionStateResponse response = new PracticeResponseDTO.SessionStateResponse();
        response.setCurrentStep(state.getCurrentStep());
        response.setMode(state.getMode());
        response.setSubStepQueue(state.getSubStepQueue());
        response.setCompletedSubSteps(state.getCompletedSubSteps());
        return response;
    }

    // ===== 5단계: 행동실험 결과 =====

    public PracticeResponseDTO.StepResponse saveBehavioralExperiment(
            String sessionId, Long userId, PracticeRequestDTO.BehavioralExperimentRequest request) {

        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);
        state.setExpectationVsReality(request.getExpectationVsReality());
        state.setCurrentStep(PracticeStep.ALTERNATIVE_THOUGHT);
        saveState(sessionId, state);

        return toStepResponse(state);
    }

    // ===== 6단계: 대체 사고 추천 =====

    public PracticeResponseDTO.StepResponse saveAlternativeThought(
            String sessionId, Long userId, PracticeRequestDTO.AlternativeThoughtRequest request) {

        PracticeSessionStateDTO state = loadOwnedState(sessionId, userId);
        state.setAlternativeThought(request.getSelectedThought());
        state.setAlternativeThoughtCustom(request.getCustomThought());
        state.setCurrentStep(PracticeStep.COMPLETED);
        saveState(sessionId, state);

        return toStepResponse(state);
    }

    // ===== 7단계: 완료 처리 (Redis → MySQL 이관) =====

    @Transactional
    public PracticeResponseDTO.CompleteResponse completeSession(User user, String sessionId) {
        PracticeSessionStateDTO state = loadOwnedState(sessionId, user.getId());
        if (state.getCurrentStep() != PracticeStep.COMPLETED) {
            throw new IllegalStateException("아직 완료되지 않은 세션입니다.");
        }

        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 입니다."));

        PracticeSession practiceSession = PracticeSession.builder()
                .id(sessionId)
                .user(persistedUser)
                .mode(state.getMode())
                .selectedThoughts(writeJson(state.getSelectedThoughts()))
                .customThought(state.getCustomThought())
                .breathingCompleted(state.getBreathingCompleted())
                .expectationVsReality(state.getExpectationVsReality())
                .alternativeThought(state.getAlternativeThought())
                .alternativeThoughtCustom(state.getAlternativeThoughtCustom())
                .build();
        practiceSessionRepository.save(practiceSession);

        List<PracticeSubStepResult> subStepResults = new ArrayList<>();
        for (PracticeSessionStateDTO.SubStepResult r : state.getSubStepResults()) {
            subStepResults.add(PracticeSubStepResult.builder()
                    .practiceSession(practiceSession)
                    .subStepType(r.getSubStep())
                    .score(r.getScore())
                    .feedbackText(r.getFeedbackText())
                    .rawResultJson(r.getRawResultJson())
                    .build());
        }
        practiceSubStepResultRepository.saveAll(subStepResults);

        persistedUser.updateStreakDays(LocalDate.now());

        redisTemplate.delete(key(sessionId));

        PracticeResponseDTO.CompleteResponse response = new PracticeResponseDTO.CompleteResponse();
        response.setSessionId(sessionId);
        response.setMode(practiceSession.getMode());
        response.setCompletedSubSteps(state.getCompletedSubSteps());
        response.setAlternativeThought(practiceSession.displayThought());
        return response;
    }

    // ===== 최종 리포트 조회 (MySQL) =====

    @Transactional
    public PracticeResponseDTO.ReportResponse getReport(Long userId, String sessionId) {
        PracticeSession practiceSession = practiceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 연습 세션입니다."));

        if (!practiceSession.getUser().getId().equals(userId)) {
            throw new NotFoundException("존재하지 않는 연습 세션입니다.");
        }

        PracticeResponseDTO.ReportResponse response = new PracticeResponseDTO.ReportResponse();
        response.setSessionId(practiceSession.getId());
        response.setCreatedAt(practiceSession.getCreatedAt());
        response.setMode(practiceSession.getMode());

        PracticeResponseDTO.ThoughtRecognitionResult thoughtRecognitionResult = new PracticeResponseDTO.ThoughtRecognitionResult();
        thoughtRecognitionResult.setSelectedThoughts(readJsonThoughts(practiceSession.getSelectedThoughts()));
        thoughtRecognitionResult.setCustomThought(practiceSession.getCustomThought());
        response.setThoughtRecognition(thoughtRecognitionResult);

        PracticeResponseDTO.BehavioralExperimentResult behavioralExperimentResult = new PracticeResponseDTO.BehavioralExperimentResult();
        behavioralExperimentResult.setExpectationVsReality(practiceSession.getExpectationVsReality());
        response.setBehavioralExperiment(behavioralExperimentResult);

        PracticeResponseDTO.AlternativeThoughtResult alternativeThoughtResult = new PracticeResponseDTO.AlternativeThoughtResult();
        alternativeThoughtResult.setSelectedThought(practiceSession.getAlternativeThought());
        alternativeThoughtResult.setCustomThought(practiceSession.getAlternativeThoughtCustom());
        response.setAlternativeThought(alternativeThoughtResult);

        List<PracticeSubStepResult> subStepResults = practiceSubStepResultRepository.findByPracticeSession(practiceSession);
        List<PracticeResponseDTO.SubStepResultResponse> subStepResultResponses = new ArrayList<>();
        for (PracticeSubStepResult r : subStepResults) {
            PracticeResponseDTO.SubStepResultResponse dto = new PracticeResponseDTO.SubStepResultResponse();
            dto.setSubStep(r.getSubStepType());
            dto.setScore(r.getScore());
            dto.setFeedbackText(r.getFeedbackText());
            dto.setRawResultJson(r.getRawResultJson());
            subStepResultResponses.add(dto);
        }
        response.setSubStepResults(subStepResultResponses);

        return response;
    }

    // ===== 내부 유틸 =====

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private void saveState(String sessionId, PracticeSessionStateDTO state) {
        redisTemplate.opsForValue().set(key(sessionId), writeJson(state), SESSION_TTL);
    }

    private PracticeSessionStateDTO loadStateOrNull(String sessionId) {
        String json = redisTemplate.opsForValue().get(key(sessionId));
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, PracticeSessionStateDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("연습 세션 조회 실패", e);
        }
    }

    private PracticeSessionStateDTO loadOwnedState(String sessionId, Long userId) {
        PracticeSessionStateDTO state = loadStateOrNull(sessionId);
        if (state == null || !state.getUserId().equals(userId)) {
            throw new NotFoundException("존재하지 않거나 만료된 연습 세션입니다.");
        }
        return state;
    }

    private PracticeType parseSubStep(String subStepRaw) {
        try {
            return PracticeType.valueOf(subStepRaw.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private PracticeResponseDTO.StepResponse toStepResponse(PracticeSessionStateDTO state) {
        PracticeResponseDTO.StepResponse response = new PracticeResponseDTO.StepResponse();
        response.setCurrentStep(state.getCurrentStep());
        response.setSubStepQueue(state.getSubStepQueue());
        return response;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    private List<springkong.talki_spring.enums.AutomaticThoughtType> readJsonThoughts(String json) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, springkong.talki_spring.enums.AutomaticThoughtType.class));
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
