package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AnalyzeService {
    private final WebClient fastApiWebClient;
    public Object forwardToFastApi(MultipartFile file, String presentationType) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        return fastApiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/analyze/record")
                        .queryParam("presentation_type", presentationType)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Object.class)
                .doOnSubscribe(s -> System.out.println("🚀 FastAPI HTTP 요청 전송"))
                .doOnError(e -> System.err.println("❌ FastAPI HTTP 오류: " + e))
                .block(); // 여기서는 block 써도 OK
    }
}
