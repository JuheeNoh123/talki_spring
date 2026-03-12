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

SERVER: {"type": "session_start", "presentationId": "94b364fe919044818558bf83665a8f2f"}


실시간 발표 진행 중 요청/응답 예시

Client → Server Message

{

  "face": { "468": {"x":0.2,"y":0.4} },
  
  "pose": { "13": {"x":0.5,"y":0.5} },
  
  "audio": "BASE64_AUDIO",
  
  "timestamp": 1712345678123
  
}

Server → Client Message

{

  "type": "feedback",
  
  "raw_result": {gaze:{...}, pose_detected:{...}, pose_landmarks:{...}, ...},
  
  "data": "시선이 불안정합니다." / null
  
}
"""
    )
    @GetMapping("/docs/websocket/realtime")
    public String websocketDocs() {
        return "Check Swagger description for WebSocket usage.";
    }
}