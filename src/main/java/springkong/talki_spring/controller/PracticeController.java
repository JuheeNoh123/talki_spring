package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.PracticeDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.service.PracticeService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Practice", description = "연습탭")
@RequestMapping("/practice")
public class PracticeController {
    private final PracticeService practiceService;

    @PostMapping("/end")
    public void practiceEnd(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                            @RequestBody PracticeDTO.PracticeDTOBuilder practiceDTOBuilder) {
        practiceService.endPracticeAndSave(customUserDetails.getUser(), practiceDTOBuilder);
    }
}
