package springkong.talki_spring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import springkong.talki_spring.domain.Feedback;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.AnalyzeResultDTO;
import springkong.talki_spring.repository.FeedbackRepository;
import springkong.talki_spring.repository.PresentationRepository;
//import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDateTime;
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

    @Transactional
    public void saveReport(AnalyzeResultDTO dto) throws JsonProcessingException {

        Presentation presentation =
                presentationRepository.findByS3Key(dto.getS3Key())
                        .orElseThrow();

        User user = presentation.getUser();

        AnalyzeResultDTO.FeedbackDTO feedbackDto = dto.getFeedback();
        AnalyzeResultDTO.ScoreDetail scoreDetail = feedbackDto.getScore_detail();
        AnalyzeResultDTO.Metrics metrics = feedbackDto.getMetrics();
        AnalyzeResultDTO.RawResultDTO raw = dto.getRawResult();


        ObjectMapper mapper = new ObjectMapper();

        // 🔥 기존 feedback 있는지 확인 (1:1)
        Feedback feedback = feedbackRepository
                .findByPresentation(presentation)
                .orElse(null);

        if (feedback == null) {
            feedback = Feedback.builder()
                    .presentation(presentation)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        // ===== 점수 =====
        feedback.setTotalScore(feedbackDto.getScore());
        feedback.setGazeScore(scoreDetail.getGaze());
        feedback.setSpeechScore(scoreDetail.getSpeech_speed());
        feedback.setPostureScore(scoreDetail.getPose());
        feedback.setFillerScore(scoreDetail.getFillers());

        // ===== KPI =====
        feedback.setSpeechWpm(metrics.getSpeech_wpm());
        feedback.setGazeFrontRatio(metrics.getGaze_front_ratio());
        feedback.setPoseWarningRatio(raw.getPose_warning_ratio());

        // ===== JSON 저장 =====
        feedback.setLlmFeedbackJson(
                mapper.writeValueAsString(feedbackDto.getLlm_feedback())
        );

        String rawJson = mapper.writeValueAsString(raw);
        feedback.setRawDataJson(rawJson);

        feedbackRepository.save(feedback);

        presentation.setStatus("DONE");
    }
}
