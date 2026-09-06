package springkong.talki_spring.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
public class FastApiWebSocketClient {

    // ✅ 싱글톤: 내부 EventLoopGroup, ChannelPool을 JVM 전체에서 공유
    //    매 connect마다 new 생성 시 → native thread 누적 → OOM
    private final ReactorNettyWebSocketClient wsClient = new ReactorNettyWebSocketClient();

    @Value("${fastapi.url}")
    private String fastApiUrl;

    // ✅ clientSessionId → FastAPI 연결 컨텍스트 (다중 세션 지원)
    //    ConcurrentHashMap: 다수 사용자 동시 접속 thread-safe 보장
    private final Map<String, FastApiConnectionContext> connections = new ConcurrentHashMap<>();

    /**
     * 클라이언트 세션에 대응하는 FastAPI WebSocket 연결 생성
     * - 각 클라이언트마다 독립된 FastAPI 연결 유지
     * - Disposable을 Context에 저장하여 lifecycle 관리
     */
    public void connectForSession(String clientSessionId, String presentationType, Consumer<String> onMessage) {
        connect(clientSessionId, "/realtime?type=" + presentationType, onMessage);
    }

    /**
     * 연습탭 4단계(하위단계 실시간 분석)용 FastAPI WebSocket 연결
     * 실전 탭과 동일한 연결/중계 로직을 재사용하고, 대상 경로만 다르다.
     */
    public void connectForPracticeSession(String clientSessionId, String practiceSessionId, String subStep, Consumer<String> onMessage) {
        connect(clientSessionId, "/practice/realtime?sessionId=" + practiceSessionId + "&subStep=" + subStep, onMessage);
    }

    private void connect(String clientSessionId, String pathAndQuery, Consumer<String> onMessage) {
        URI uri = URI.create(fastApiUrl.replaceFirst("^http", "ws") + pathAndQuery);

        FastApiConnectionContext context = new FastApiConnectionContext(clientSessionId);
        connections.put(clientSessionId, context);

        Mono<Void> connectionMono = wsClient.execute(uri, session -> {
            log.info("[{}] FastAPI WebSocket connected", clientSessionId);
            context.setFastApiSession(session);

            // FastAPI → Client: receive stream
            Mono<Void> receive = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(msg -> {
                        log.debug("[FastAPI→Spring][{}] {}", clientSessionId, msg);
                        onMessage.accept(msg);
                    })
                    .doOnError(e -> log.error("[{}] Receive stream error", clientSessionId, e))
                    .then();

            // Client → FastAPI: Sinks.Many 기반 send stream
            // fire-and-forget subscribe 없이 단일 Flux가 모든 메시지 처리
            Mono<Void> send = session.send(
                    context.getSendSink().asFlux()
                            .map(session::textMessage)
            );

            // receive OR send 중 하나라도 종료되면 연결 종료 + cleanup
            // Mono.when: 양쪽 모두 종료될 때 완료 (정상 CLOSE handshake)
            // 연결 오류 시 어느 쪽이든 error → 즉시 전파 → doFinally 호출
            return Mono.when(receive, send)
                    .doFinally(signal -> {
                        log.info("[{}] FastAPI connection finalized: {}", clientSessionId, signal);
                        connections.remove(clientSessionId);
                    });
        }).doOnError(e -> {
            log.error("[{}] FastAPI connection failed", clientSessionId, e);
            connections.remove(clientSessionId);
        });

        // ✅ Disposable 저장: 세션 종료 시 context.close()에서 dispose() 호출 가능
        context.setConnectionDisposable(
                connectionMono.subscribe(
                        null,
                        e -> log.error("[{}] Connection subscription error", clientSessionId, e),
                        () -> log.info("[{}] Connection subscription completed", clientSessionId)
                )
        );
    }

    /**
     * 해당 클라이언트 세션의 FastAPI 연결로 메시지 전송
     * Sinks.Many에 emit → 단일 send 스트림이 소비 (fire-and-forget 없음)
     */
    public void sendToFastApi(String clientSessionId, String json) {
        FastApiConnectionContext context = connections.get(clientSessionId);
        if (context != null) {
            context.send(json);
        } else {
            log.warn("[{}] sendToFastApi: no active FastAPI connection", clientSessionId);
        }
    }

    /**
     * 클라이언트 세션 종료 시 FastAPI 연결 해제
     * afterConnectionClosed / handleTransportError 에서 반드시 호출
     */
    public void disconnectForSession(String clientSessionId) {
        FastApiConnectionContext context = connections.remove(clientSessionId);
        if (context != null) {
            context.close();
        } else {
            log.warn("[{}] disconnectForSession: no connection to close", clientSessionId);
        }
    }

    /** 모니터링용: 현재 활성 연결 수 */
    public int activeConnectionCount() {
        return connections.size();
    }
}