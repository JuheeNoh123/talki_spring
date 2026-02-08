package springkong.talki_spring.websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.socket.WebSocketMessage;
import java.net.URI;

@Service
public class FastApiWebSocketClient {
    private WebSocketSession fastApiSession;

    public Mono<Void> connect(String presentationType) {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

        URI uri = URI.create(
                "wss://curelessly-unusable-jolanda.ngrok-free.dev:8000/realtime?type=" + presentationType
        );

        return client.execute(uri, session -> {
            this.fastApiSession = session;

            return session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(msg -> {
                        System.out.println("[FastAPI → Spring] " + msg);
                    })
                    .then();
        });
    }

    public void sendToFastApi(String json) {
        System.out.println("[Spring → FastAPI] " + json);
        if (fastApiSession != null && fastApiSession.isOpen()) {
            fastApiSession.send(
                    Mono.just(fastApiSession.textMessage(json))
            ).subscribe();
        }
    }
}
