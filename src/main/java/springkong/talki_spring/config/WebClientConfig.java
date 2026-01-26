package springkong.talki_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

//FastAPI에 요청을 보내야 함
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient fastApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(5)); //타임아웃 대비 FastAPI 응답을 최대 5분까지 기다린다
        return WebClient.builder()
                .baseUrl("http://localhost:8000")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
