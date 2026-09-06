package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Practice Realtime Analysis WebSocket")
public class PracticeWebSocketSwaggerController {

    @Operation(
            summary = "연습탭 4단계 하위단계 실시간 분석 WebSocket",
            description = """
WebSocket Endpoint

ws://43.201.182.246:8080/practice/realtime?sessionId={practiceSessionId}&subStep={SCRIPT|GAZE|IMPROMPTU|KEYWORD|POINT}

- practiceSessionId: POST /practice/sessions로 발급받은 세션 ID
- subStep: PATCH /practice/sessions/{sessionId}/mode 응답의 subStepQueue에 담긴 값 중 하나. 큐에 없거나 이미 완료된 subStep으로 연결하면 즉시 에러 메시지 후 연결이 닫힙니다.

연결 실패 시 응답
```
{ "type": "error", "message": "invalid or already completed subStep: SCRIPT" }
```

연결 성공 시 최초 1회, FastAPI가 하위단계별 컨텍스트를 내려줍니다.

session_start 예시 (SCRIPT, 실측 확인됨)
```
{
  "type": "session_start",
  "subStep": "SCRIPT",
  "script_text": "안녕하세요. 오늘은 짧지만 중요한 이야기를 해보려고 합니다. ...",
  "speak_seconds": 60,
  "reference_range": { "wpm_min": 120, "wpm_max": 160 }
}
```

session_start 예시 (GAZE, 실측 확인됨)
```
{ "type": "session_start", "subStep": "GAZE", "target_duration_sec": 30 }
```

session_start 예시 (IMPROMPTU, 실측 확인됨 — 연결 즉시 OpenAI API 호출로 주제 생성, 실제 과금 발생)
```
{
  "type": "session_start",
  "subStep": "IMPROMPTU",
  "topic": "최근에 가본 카페 중에서 가장 기억에 남는 곳은 어디인가요?",
  "prep_seconds": 10,
  "speak_seconds": 30
}
```

session_start 예시 (KEYWORD / POINT, ⚠️ 아직 미검증 — 가정)
```
{ "type": "session_start", "subStep": "KEYWORD", "keywords": ["...", "...", "..."] }
{ "type": "session_start", "subStep": "POINT", "passage": "..." }
```

클라이언트 → 서버 실시간 스트리밍 (실전 탭과 동일한 payload)

음성 위주 하위단계(SCRIPT, IMPROMPTU, KEYWORD, POINT)
```
{ "audio": "BASE64_AUDIO", "timestamp": 1712345678123 }
```

GAZE (얼굴 랜드마크)
```
{ "face": { "468": {"x":0.2,"y":0.4} }, "timestamp": 1712345678123 }
```

서버 → 클라이언트: 진행 중 라이브 피드백 (매 프레임/구간마다, 없으면 오지 않을 수 있음)
```
{ "type": "feedback", "subStep": "SCRIPT", "data": ["말 속도가 조금 빠릅니다."] }
```

서버 → 클라이언트: 하위단계 최종 결과 (1회, 실측 확인됨)
```
{
  "type": "result",
  "subStep": "SCRIPT",
  "score": 30,
  "feedback_text": "말 속도가 다소 느립니다. 발음은 미흡 수준입니다.",
  "raw_result": {
    "wpm": 0.0,
    "fillers_count": 0,
    "fillers_freq": 0.0,
    "duration_sec": 0.0,
    "articulation_label": "미흡",
    "text": ""
  }
}
```

⚠️ 주의: result 메시지를 받아도 FastAPI가 먼저 연결을 끊지 않습니다. 다음 하위단계로 넘어가거나 4단계를
마칠 때 FE가 직접 ws.close()를 호출해야 합니다.
"""
    )
    @GetMapping("/docs/websocket/practice-realtime")
    public String websocketDocs() {
        return "Check Swagger description for WebSocket usage.";
    }
}
