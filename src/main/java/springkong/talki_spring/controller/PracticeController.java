package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.PracticeRequestDTO;
import springkong.talki_spring.dto.response.PracticeResponseDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.service.PracticeService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Practice", description = "연습탭 (CBT 기반 6단계 위저드). 전 구간 로그인 필요(Authorization: Bearer {accessToken})")
@RequestMapping("/practice/sessions")
public class PracticeController {
    private final PracticeService practiceService;

    @Operation(
            summary = "0단계: 훈련 시작",
            description = """
연습 세션을 새로 생성합니다.

- 세션 상태는 Redis에만 저장되며 TTL은 30분입니다.
- 위저드 진행 중 이탈하면 세션은 폐기됩니다 (재개 불가). 재접속 시 이 API를 다시 호출해 0단계부터 시작하세요.

응답 예시
```
{
  "sessionId": "6f1a9d2e-6b7b-4c3a-9c34-5e2b7d6a1a10",
  "currentStep": "THOUGHT_RECOGNITION"
}
```
""")
    @PostMapping
    public PracticeResponseDTO.SessionStartResponse startSession(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return practiceService.startSession(userDetails.getUser());
    }

    @Operation(
            summary = "1단계: 자동사고 인식",
            description = """
부정적 자동사고 항목을 복수 선택하고, 직접 입력한 생각을 함께 저장합니다.

selectedThoughts 값
- FEAR_OF_JUDGEMENT (남들이 이상하게 볼 것 같은 두려움)
- FEAR_OF_FAILURE (실패에 대한 두려움)
- FEAR_OF_BLANKING (머릿속이 하얘질 것 같은 두려움)
- FEAR_OF_BORING_AUDIENCE (지루하게 느낄까봐 걱정)
- PHYSICAL_ANXIETY (신체적 긴장/불안 증상)
- OTHER (기타, customThought에 자유 입력)

요청 예시
```
{ "selectedThoughts": ["FEAR_OF_JUDGEMENT", "FEAR_OF_BLANKING"], "customThought": "말이 막히면 어떡하지" }
```
""")
    @PatchMapping("/{sessionId}/thought-recognition")
    public PracticeResponseDTO.StepResponse thoughtRecognition(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId,
            @RequestBody PracticeRequestDTO.ThoughtRecognitionRequest request) {
        return practiceService.saveThoughtRecognition(sessionId, userDetails.getUserId(), request);
    }

    @Operation(
            summary = "2단계: 호흡 조절",
            description = """
10초 호흡 유도 애니메이션 완료 여부만 기록합니다. (별도 분석 데이터 없음)

요청 예시
```
{ "completed": true }
```
""")
    @PatchMapping("/{sessionId}/breathing")
    public PracticeResponseDTO.StepResponse breathing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId,
            @RequestBody PracticeRequestDTO.BreathingRequest request) {
        return practiceService.saveBreathing(sessionId, userDetails.getUserId(), request);
    }

    @Operation(
            summary = "3단계: 연습 모드 선택",
            description = """
연습 모드를 선택합니다. (스킵 가능)

mode 값
- SCRIPT_BASED : 스크립트 기반 기초 연습 (약 8분). 하위단계 = [SCRIPT, GAZE]
- IMPROMPTU : 즉흥 구성 연습 (약 10분). 하위단계 = [IMPROMPTU, KEYWORD, POINT]
- SKIPPED : 4단계를 건너뛰고 바로 5단계(행동실험)로 이동

요청 예시
```
{ "mode": "SCRIPT_BASED" }
```

응답의 subStepQueue 순서대로 4단계에서 WebSocket(/practice/realtime)에 연결하면 됩니다.
mode가 SKIPPED이면 subStepQueue는 빈 배열이고 currentStep이 바로 BEHAVIORAL_EXPERIMENT로 넘어갑니다.
""")
    @PatchMapping("/{sessionId}/mode")
    public PracticeResponseDTO.StepResponse selectMode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId,
            @RequestBody PracticeRequestDTO.ModeRequest request) {
        return practiceService.selectMode(sessionId, userDetails.getUserId(), request);
    }

    @Operation(
            summary = "4단계 진행 상태 조회",
            description = """
WebSocket(/practice/realtime)으로 진행되는 4단계(연습 진행)의 현재 상태를 FE가 동기화할 때 사용합니다.
WebSocket 연결 프로토콜 자체는 `/docs/websocket/practice-realtime` 문서를 참고하세요.

completedSubSteps에 subStepQueue의 모든 항목이 포함되면 currentStep이 자동으로 BEHAVIORAL_EXPERIMENT로 전환됩니다.

응답 예시
```
{
  "currentStep": "PRACTICE_IN_PROGRESS",
  "mode": "SCRIPT_BASED",
  "subStepQueue": ["SCRIPT", "GAZE"],
  "completedSubSteps": ["SCRIPT"]
}
```
""")
    @GetMapping("/{sessionId}")
    public PracticeResponseDTO.SessionStateResponse getSessionState(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId) {
        return practiceService.getSessionState(sessionId, userDetails.getUserId());
    }

    @Operation(
            summary = "5단계: 행동실험 결과",
            description = """
직접 말해본 후 예상과 실제를 비교하는 단일 선택 응답입니다.

expectationVsReality 값
- MUCH_WORSE_THAN_EXPECTED
- WORSE_THAN_EXPECTED
- AS_EXPECTED
- BETTER_THAN_EXPECTED
- MUCH_BETTER_THAN_EXPECTED

요청 예시
```
{ "expectationVsReality": "BETTER_THAN_EXPECTED" }
```
""")
    @PatchMapping("/{sessionId}/behavioral-experiment")
    public PracticeResponseDTO.StepResponse behavioralExperiment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId,
            @RequestBody PracticeRequestDTO.BehavioralExperimentRequest request) {
        return practiceService.saveBehavioralExperiment(sessionId, userDetails.getUserId(), request);
    }

    @Operation(
            summary = "6단계: 대체 사고 추천",
            description = """
마무리 긍정/대체사고 문장을 선택하거나 직접 입력합니다.

selectedThought 값
- PRACTICE_MAKES_IT_BETTER (연습할수록 나아진다)
- MISTAKES_ARE_OKAY (실수해도 괜찮다)
- AUDIENCE_IS_ON_MY_SIDE (청중은 내 편이다)
- CUSTOM (직접 입력, customThought 필드에 문장을 담아 보내주세요)

요청 예시
```
{ "selectedThought": "PRACTICE_MAKES_IT_BETTER", "customThought": null }
```
""")
    @PatchMapping("/{sessionId}/alternative-thought")
    public PracticeResponseDTO.StepResponse alternativeThought(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId,
            @RequestBody PracticeRequestDTO.AlternativeThoughtRequest request) {
        return practiceService.saveAlternativeThought(sessionId, userDetails.getUserId(), request);
    }

    @Operation(
            summary = "7단계: 완료 처리",
            description = """
연습 세션을 완료 처리합니다.

- Redis에 쌓인 세션 전체(1~6단계 응답 + 4단계 하위단계 결과)를 MySQL(PracticeSession, PracticeSubStepResult)로 이관합니다.
- 사용자 스트릭(연속 연습일수)을 갱신합니다.
- Redis 세션을 삭제합니다. 이후 GET /practice/sessions/{sessionId}는 404를 반환합니다.
- 6단계까지 마치지 않은 세션(currentStep != COMPLETED)에 대해 호출하면 오류가 발생합니다.

응답 예시
```
{
  "sessionId": "6f1a9d2e-6b7b-4c3a-9c34-5e2b7d6a1a10",
  "mode": "SCRIPT_BASED",
  "completedSubSteps": ["SCRIPT", "GAZE"],
  "alternativeThought": "PRACTICE_MAKES_IT_BETTER"
}
```
""")
    @PostMapping("/{sessionId}/complete")
    public PracticeResponseDTO.CompleteResponse complete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId) {
        return practiceService.completeSession(userDetails.getUser(), sessionId);
    }

    @Operation(
            summary = "완료된 연습 세션의 최종 리포트 조회",
            description = """
7단계(complete) 처리가 끝난 세션에 대해서만 조회 가능합니다. (MySQL 기준)

응답 예시
```
{
  "sessionId": "6f1a9d2e-6b7b-4c3a-9c34-5e2b7d6a1a10",
  "createdAt": "2026-09-06T16:19:14",
  "mode": "SCRIPT_BASED",
  "thoughtRecognition": { "selectedThoughts": ["FEAR_OF_JUDGEMENT"], "customThought": null },
  "behavioralExperiment": { "expectationVsReality": "BETTER_THAN_EXPECTED" },
  "alternativeThought": { "selectedThought": "PRACTICE_MAKES_IT_BETTER", "customThought": null },
  "subStepResults": [
    {
      "subStep": "SCRIPT",
      "score": 82,
      "feedbackText": "속도가 안정적이었어요.",
      "rawResultJson": "{ \\"wpm\\": 128, \\"fillers_count\\": 3 }"
    }
  ]
}
```
""")
    @GetMapping("/{sessionId}/report")
    public PracticeResponseDTO.ReportResponse report(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String sessionId) {
        return practiceService.getReport(userDetails.getUserId(), sessionId);
    }
}
