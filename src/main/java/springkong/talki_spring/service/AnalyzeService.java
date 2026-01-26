package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
