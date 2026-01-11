package springkong.talki_spring.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springkong.talki_spring.dto.FeedbackEvent;
import springkong.talki_spring.service.FeedbackService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presentation")
public class dataController {
    private final FeedbackService feedbackService;

    @GetMapping("/{presentationId}/feedbacks")
    public List<FeedbackEvent> getFeedbacks(
            @PathVariable String presentationId
    ) {
        return feedbackService.getFeedbacks(presentationId);
    }
}
