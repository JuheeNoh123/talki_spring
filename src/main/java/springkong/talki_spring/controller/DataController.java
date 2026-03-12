package springkong.talki_spring.controller;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import springkong.talki_spring.dto.request.AnalyzeResultDTO;
import springkong.talki_spring.dto.request.FeedbackEventDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.service.AnalyzeService;
import springkong.talki_spring.service.FeedbackService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Analyze", description = "발표 분석 API")
public class DataController {
    private final FeedbackService feedbackService;
    private final AnalyzeService analyzeService;
    private final WebClient fastApiWebClient;



    @Operation(summary = "FastAPI 서버 상태 확인",
            description ="연결 성공시 {\"status\":\"ok\"} 반환")
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
//    @Operation(summary = "실시간 발표 피드백 조회",
//            description = "presentationId는 WebSocket 연결 시 전달받은 session_start 메시지의 presentationId 값을 사용해야 합니다.")
//    @GetMapping("/presentation/{presentationId}/feedbacks")
//    public ResponseEntity<List<FeedbackEventDTO>> getFeedbacks(
//            @Parameter(description = "WebSocket 연결 시 받은 presentationId 사용")
//            @PathVariable String presentationId
//    ) {
//        return ResponseEntity.ok(feedbackService.getRealTimeFeedbacks(presentationId));
//    }

    //녹화 영상 보내기
    @Operation(summary = "S3 영상 분석 시작",
            description = "실시간 실전 연습 종료 시 녹화된 S3 영상 전체 분석 API")
    @PostMapping("/analyze/start")
    public ResponseEntity<?> startAnalyze(
            @RequestParam String key,
            @Parameter(
                    description = "발표 유형 (online_small, small, large 중 하나)",
                    example = "online_small"
            )
            @RequestParam(defaultValue = "online_small") String presentationType
    ) {
        return ResponseEntity.ok(
                analyzeService.analyzeFromS3(key, presentationType)
        );
    }

    @Hidden
    @PostMapping("/analyze/callback")
    //@Operation(summary = "FastAPI 분석 결과 콜백",
    //       description = "FastAPI 영상 분석 완료 후 Spring 서버로 결과를 전달하는 내부 API입니다. 프론트엔드에서는 호출하지 않습니다.")
    public ResponseEntity<?> saveFeedback(@RequestBody AnalyzeResultDTO dto) throws Exception {
        System.out.println(dto);
        System.out.println("🔥 CALLBACK 도착");
        analyzeService.saveReport(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "영상 결과 조회")
    @GetMapping("/analyze/getResult")
    public ResponseEntity<?> getResult(@AuthenticationPrincipal CustomUserDetails user, String presentationId) {
        Long userId = (user != null) ? user.getUserId() : null;
        return ResponseEntity.ok(
                feedbackService.getFeedbacks(userId, presentationId)
        );
    }

}
