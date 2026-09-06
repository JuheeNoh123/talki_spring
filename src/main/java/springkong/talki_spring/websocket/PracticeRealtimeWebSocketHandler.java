package springkong.talki_spring.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springkong.talki_spring.service.PracticeService;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 연습탭 4단계(연습 진행) 하위단계 실시간 분석용 WebSocket 핸들러.
 * 실전 탭 RealtimeWebSocketHandler와 동일하게 클라이언트 ↔ FastAPI를 얇게 중계하되,
 * FastAPI가 보내는 "result" 메시지는 가로채어 연습 세션(Redis) 상태에 반영한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeRealtimeWebSocketHandler extends TextWebSocketHandler {

    private final FastApiWebSocketClient fastApiClient;
    private final PracticeService practiceService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        Map<String, String> query = parseQuery(session.getUri());
        String practiceSessionId = query.get("sessionId");
        String subStep = query.get("subStep");

        if (practiceSessionId == null || subStep == null) {
            sendErrorAndClose(session, "sessionId and subStep are required");
            return;
        }

        if (!practiceService.isSubStepPending(practiceSessionId, subStep)) {
            sendErrorAndClose(session, "invalid or already completed subStep: " + subStep);
            return;
        }

        log.info("[{}] Practice client connected, practiceSessionId={}, subStep={}", sessionId, practiceSessionId, subStep);

        fastApiClient.connectForPracticeSession(sessionId, practiceSessionId, subStep, msg -> {
            captureResultIfPresent(practiceSessionId, subStep, msg);
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (Exception e) {
                log.error("[{}] Failed to forward FastAPI message to client", sessionId, e);
            }
        });
    }

    private void captureResultIfPresent(String practiceSessionId, String subStep, String msg) {
        try {
            JsonNode node = objectMapper.readTree(msg);
            if ("result".equals(node.path("type").asText())) {
                practiceService.appendSubStepResult(practiceSessionId, subStep, node);
            }
        } catch (Exception e) {
            log.warn("Failed to parse FastAPI message for subStep result capture: {}", msg, e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        fastApiClient.sendToFastApi(session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[{}] Practice client disconnected: {}", session.getId(), status);
        fastApiClient.disconnectForSession(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[{}] Practice transport error", session.getId(), exception);
        fastApiClient.disconnectForSession(session.getId());
    }

    private void sendErrorAndClose(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + message + "\"}"));
        session.close();
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        if (uri == null || uri.getQuery() == null) return result;
        for (String pair : uri.getQuery().split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }
}
