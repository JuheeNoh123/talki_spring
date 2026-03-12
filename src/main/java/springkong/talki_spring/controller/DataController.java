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

    @Operation(
            summary = "영상 분석 결과 조회",
            description = """
발표 영상의 분석 결과를 조회하는 API입니다.

### 요청
GET /analyze/getResult

Query Parameter
- presentationId : 분석할 발표 ID

### 인증
- Authorization 헤더에 JWT 토큰을 **넣어도 되고, 안 넣어도 됩니다.**
- 토큰 존재 여부로 회원 여부를 판단합니다.

### 사용자 유형별 응답

1. 비회원 (토큰 없음)
- 공통 분석 결과만 반환됩니다.
- 조회 후 해당 발표 데이터는 서버에서 삭제됩니다. (영상 파일은 유지)

2. 회원 BASIC (토큰 있음)
- 공통 분석 결과만 반환됩니다.

3. 회원 PREMIUM (토큰 있음)
- 공통 분석 결과 + 약점 구간 리스트(realTimeResultDTO)가 반환됩니다.

### 약점 구간 타입
realTimeResultDTO.type 값

- pose_rigid : 자세가 경직된 구간
- pose_unstable : 자세가 불안정한 구간
- gaze_unstable : 시선이 불안정한 구간
- speech_slow : 말 속도가 느린 구간
- speech_fast : 말 속도가 빠른 구간
- silence : 침묵 구간
"""
    )
    @GetMapping("/analyze/getResult")
    public ResponseEntity<?> getResult(@AuthenticationPrincipal CustomUserDetails user, String presentationId) {
        Long userId = (user != null) ? user.getUserId() : null;
        return ResponseEntity.ok(
                feedbackService.getFeedbacks(userId, presentationId)
        );
    }

}
