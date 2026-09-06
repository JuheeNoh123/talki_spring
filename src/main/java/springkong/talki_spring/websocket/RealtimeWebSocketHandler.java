package springkong.talki_spring.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final FastApiWebSocketClient fastApiClient;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();

        if (session.getUri() == null || session.getUri().getQuery() == null) {
            sendErrorAndClose(session, "presentation type is required");
            return;
        }

        String query = session.getUri().getQuery();
        if (!query.contains("type=")) {
            sendErrorAndClose(session, "presentation type is required");
            return;
        }

        String presentationType = query.split("type=")[1];
        if (!isValidType(presentationType)) {
            sendErrorAndClose(session, "invalid presentation type: " + presentationType);
            return;
        }

        log.info("[{}] Client connected, type={}", sessionId, presentationType);

        // ✅ sessionId 기반으로 FastAPI 연결 생성 (다중 세션 안전)
        fastApiClient.connectForSession(sessionId, presentationType, msg -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (Exception e) {
                log.error("[{}] Failed to forward FastAPI message to client", sessionId, e);
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // ✅ sessionId로 해당 사용자의 FastAPI 연결에만 전달
        fastApiClient.sendToFastApi(session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        log.info("[{}] Client disconnected: {}", sessionId, status);

        // ✅ 반드시 cleanup: FastAPI 연결 해제 + stream dispose + 맵 제거
        fastApiClient.disconnectForSession(sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        log.error("[{}] Transport error", sessionId, exception);

        // ✅ 에러 시에도 동일한 cleanup 경로
        fastApiClient.disconnectForSession(sessionId);
    }

    private void sendErrorAndClose(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage(
                "{\"type\":\"error\",\"message\":\"" + message + "\"}"
        ));
        session.close();
    }

    private boolean isValidType(String type) {
        return type.equals("small")
                || type.equals("large")
                || type.equals("online_small");
    }
}