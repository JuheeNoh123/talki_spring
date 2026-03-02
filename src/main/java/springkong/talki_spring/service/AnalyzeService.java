package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import springkong.talki_spring.domain.Feedback;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.dto.AnalyzeResultDTO;
import springkong.talki_spring.repository.FeedbackRepository;
import springkong.talki_spring.repository.PresentationRepository;
import tools.jackson.databind.ObjectMapper;

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
    private final PresentationRepository presentationRepository;
    private final FeedbackRepository feedbackRepository;

    public String analyzeFromS3(String key, String presentationType) {

        Presentation presentation =
                presentationRepository.findByS3Key(key)
                        .orElseThrow();
        presentation.setStatus("ANALYZING");

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
                        "s3_key", key,
                        "presentation_type", presentationType
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void saveReport(AnalyzeResultDTO.ResultDTO dto){
        Presentation presentation =
                presentationRepository.findByS3Key(dto.getS3Key())
                        .orElseThrow();

        ObjectMapper mapper = new ObjectMapper();

        String rawJson = mapper.writeValueAsString(dto.getRawResult());
        String feedbackJson = mapper.writeValueAsString(dto.getFeedback());

        Object scoreObj = dto.getFeedback().get("score");

        Integer score = null;

        if (scoreObj instanceof Integer) {
            score = (Integer) scoreObj;
        } else if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).intValue();
        }

        Feedback feedback = Feedback.builder()
                .presentation(presentation)
                .totalScore(score)
                .rawJson(rawJson)
                .feedbackJson(feedbackJson)
                .build();

        feedbackRepository.save(feedback);

        presentation.setStatus("DONE");
    }
}
