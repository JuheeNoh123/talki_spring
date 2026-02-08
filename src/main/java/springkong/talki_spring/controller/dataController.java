package springkong.talki_spring.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springkong.talki_spring.dto.FeedbackEvent;
import springkong.talki_spring.dto.Test;
import springkong.talki_spring.service.AnalyzeService;
import springkong.talki_spring.service.FeedbackService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class dataController {
    private final FeedbackService feedbackService;
    private final AnalyzeService analyzeService;
    @GetMapping("/analyze/get")
    public String testGet() {
        return "GET OK";
    }

    @PostMapping("/analyze/get")
    public String testPost(@RequestBody Test test) {
        return test.getName();
    }
    //발표 피드백 조회
    @GetMapping("/presentation/{presentationId}/feedbacks")
    public List<FeedbackEvent> getFeedbacks(
            @PathVariable String presentationId
    ) {
        return feedbackService.getFeedbacks(presentationId);
    }

    //녹화 영상 보내기
    @PostMapping(value = "/analyze/record", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeRecord(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "online_small") String presentationType) {
        return ResponseEntity.ok(
                analyzeService.forwardToFastApi(file, presentationType)
        );
//        analyzeService.forwardToFastApi(file, presentationType);
//
//        return ResponseEntity.ok(
//                "analysis started"
//        );
    }
}
