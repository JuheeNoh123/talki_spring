package springkong.talki_spring.websocket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
public class FastApiConnectionContext {

    private final String clientSessionId;

    @Getter
    private final Sinks.Many<String> sendSink;

    private volatile WebSocketSession fastApiSession;
    private volatile Disposable connectionDisposable;

    public FastApiConnectionContext(String clientSessionId) {
        this.clientSessionId = clientSessionId;
        // bounded buffer: 256개 초과 시 DROP (실시간 시스템 - 메모리에 쌓지 않음)
        this.sendSink = Sinks.many().unicast()
                .onBackpressureBuffer(new ArrayBlockingQueue<>(256));
    }

    public void setFastApiSession(WebSocketSession session) {
        this.fastApiSession = session;
    }

    public void setConnectionDisposable(Disposable disposable) {
        this.connectionDisposable = disposable;
    }

    /**
     * Client → FastAPI 메시지 전송
     * Sinks.Many 기반: fire-and-forget subscribe 없이 단일 send 스트림이 소비
     */
    public void send(String message) {
        Sinks.EmitResult result = sendSink.tryEmitNext(message);
        if (result.isFailure()) {
            // 실시간 시스템: BLOCK보다 DROP이 낫다 (레이턴시 우선)
            log.warn("[{}] Message dropped (backpressure/closed): {}", clientSessionId, result);
        }
    }

    /**
     * 연결 종료: 3단계 방어적 cleanup
     */
    public void close() {
        log.info("[{}] Initiating FastAPI connection close", clientSessionId);

        // 1단계: sink 완료 → send Mono 완료 → WebSocket CLOSE frame 전송
        sendSink.tryEmitComplete();

        // 2단계: 세션 명시적 종료 (CLOSE frame 미응답 대비)
        if (fastApiSession != null && fastApiSession.isOpen()) {
            fastApiSession.close()
                    .doOnError(e -> log.warn("[{}] FastAPI session close error", clientSessionId, e))
                    .subscribe();
        }

        // 3단계: subscription 강제 취소 (최후 수단)
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }
}