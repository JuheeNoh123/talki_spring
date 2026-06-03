package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Realtime Analysis WebSocket")
public class WebSocketSwaggerController {

    @Operation(
            summary = "Realtime 발표 분석 WebSocket",
            description = """
WebSocket Endpoint

ws://43.201.182.246:8080/realtime?type={presentationType}

presentationType 값

- small
- large
- online_small

Example

ws://43.201.182.246:8080/realtime?type=large


초기 연결 시 응답 예시

1. 연결 시작 (1회)

{
  "type": "session_start",
  "presentationId": "8bb25e8b2e1f415b993e54e83d044ef3"
}


실시간 발표 진행 중 요청/응답 예시

Client → Server Message

{

  "face": { "468": {"x":0.2,"y":0.4} },
  
  "pose": { "13": {"x":0.5,"y":0.5} },
  
  "audio": "BASE64_AUDIO",
  
  "timestamp": 1712345678123
  
}

Server → Client Message

2. 실시간 피드백 (매 프레임마다)

{
  "type": "feedback",
  "raw_result": {
    "gaze": { ... },
    "pose_detected": true,
    "pose_landmarks": { ... },
    "speech": { "wpm": 120, "silence": false }
  },
  "data": ["시선이 불안정합니다. 아래쪽을 자주 보고 있습니다."]
}
- data: 피드백 메시지 배열, 없으면 []

3. 돌발 질문 (발표 중 조건 충족 시)

{
  "type": "surprise_question",
  "question_id": "9cca2dda97634dab8318ab3b5ce87b5e",
  "question": "발표에서 가장 중요하게 전달하고자 한 핵심 메시지는 무엇인가요?",
  "time_limit": 30
}
"""
    )
    @GetMapping("/docs/websocket/realtime")
    public String websocketDocs() {
        return "Check Swagger description for WebSocket usage.";
    }
}