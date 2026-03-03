package springkong.talki_spring.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import springkong.talki_spring.dto.AnalyzeResultDTO;
import springkong.talki_spring.dto.FeedbackEventDTO;
import springkong.talki_spring.service.AnalyzeService;
import springkong.talki_spring.service.FeedbackService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class dataController {
    private final FeedbackService feedbackService;
    private final AnalyzeService analyzeService;
    private final WebClient fastApiWebClient;


    @GetMapping("/analyze/get")
    public String testGet() {
        return fastApiWebClient
                .get()                           // ✅ GET
                .uri("/health")                  // ✅ FastAPI health
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //발표 피드백 조회
    @GetMapping("/presentation/{presentationId}/feedbacks")
    public List<FeedbackEventDTO> getFeedbacks(
            @PathVariable String presentationId
    ) {
        return feedbackService.getFeedbacks(presentationId);
    }

    //녹화 영상 보내기
    @PostMapping("/analyze/start")
    public ResponseEntity<?> startAnalyze(
            @RequestParam String key,
            @RequestParam(defaultValue = "online_small") String presentationType
    ) {
        return ResponseEntity.ok(
                analyzeService.analyzeFromS3(key, presentationType)
        );
    }

    @PostMapping("/analyze/callback")
    public ResponseEntity<?> saveFeedback(@RequestBody AnalyzeResultDTO dto) throws Exception {
        System.out.println(dto);
        System.out.println("🔥 CALLBACK 도착");
        analyzeService.saveReport(dto);
        return ResponseEntity.ok().build();
    }

}
