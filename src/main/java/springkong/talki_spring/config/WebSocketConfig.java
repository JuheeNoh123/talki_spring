package springkong.talki_spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import springkong.talki_spring.websocket.RealtimeWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //System.out.println("🔥 registerWebSocketHandlers called");
        registry.addHandler(handler, "/realtime")
                .setAllowedOrigins("*");


    }
//    @Bean //json 크기 설정
//    public ServletServerContainerFactoryBean createWebSocketContainer() {
//        ServletServerContainerFactoryBean container =
//                new ServletServerContainerFactoryBean();
//
//        container.setMaxTextMessageBufferSize(10 * 1024 * 1024); // 10MB
//        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
//        container.setAsyncSendTimeout(20_000L);
//
//        return container;
//    }
}