package springkong.talki_spring.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler { //presentation type 받아서 넘기기

    private final FastApiWebSocketClient fastApiClient;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (session.getUri() == null || session.getUri().getQuery() == null) {
            sendErrorAndClose(session, "presentation type is required");
            return;
        }

        String query = session.getUri().getQuery(); // 예: type=large

        if (!query.contains("type=")) {
            sendErrorAndClose(session, "presentation type is required");
            return;
        }

        String presentationType = query.split("type=")[1];

        // (선택) 허용 타입 검증
        if (!isValidType(presentationType)) {
            sendErrorAndClose(session, "invalid presentation type: " + presentationType);
            return;
        }

        // ✅ 여기까지 왔으면 정상
        fastApiClient.connect(presentationType).subscribe();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        //System.out.println("🔥 handleTextMessage called");
        // Client → Spring → FastAPI
        fastApiClient.sendToFastApi(message.getPayload());
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