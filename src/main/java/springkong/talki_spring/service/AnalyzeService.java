package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.util.Map;

//@Service
//@RequiredArgsConstructor
//public class AnalyzeService {
//    private final WebClient fastApiWebClient;
//
//
//    public Object forwardToFastApi(MultipartFile file, String presentationType) {
//        try {
//            // 🔥 1. MultipartFile → 실제 파일로 변환
//            File temp = File.createTempFile("upload-", ".tmp");
//            file.transferTo(temp);
//
//            MultipartBodyBuilder builder = new MultipartBodyBuilder();
//
//            // 🔥 2. 반드시 FileSystemResource 사용
//            builder.part(
//                    "file",
//                    new FileSystemResource(temp)
//            ).filename(file.getOriginalFilename());
//
//            return fastApiWebClient.post()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/analyze/record")
//                            .queryParam("presentation_type", presentationType)
//                            .build()
//                    )
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .body(BodyInserters.fromMultipartData(builder.build()))
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .doOnSubscribe(s -> System.out.println("🚀 FastAPI HTTP 요청 전송"))
//                    .doOnSuccess(res -> System.out.println("✅ FastAPI 응답 수신"))
//                    .doOnError(err -> {
//                        System.out.println("❌ FastAPI HTTP 오류");
//                        err.printStackTrace();
//                    })
//                    .block();
//
//        } catch (Exception e) {
//            throw new RuntimeException(
//                    "FastAPI 분석 요청 실패",
//                    e
//            );
//        }
//    }
//}


@Service
@RequiredArgsConstructor
public class AnalyzeService {

    private final WebClient fastApiWebClient;
    private final S3Service s3Service;

    public String analyzeFromS3(String key, String presentationType) {

        // 1️⃣ presigned GET URL 생성
        String downloadUrl = s3Service.generateDownloadUrl(key);

        // 2️⃣ FastAPI로 URL 전달
        return fastApiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/analyze/record-from-s3")
                        .queryParam("presentation_type", presentationType)
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "video_url", downloadUrl,
                        "s3_key", key
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
