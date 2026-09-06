package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springkong.talki_spring.dto.response.HomeResponseDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.service.HomeService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Home", description = "개인화면 - 홈")
@RequestMapping("/personal")
public class PersonalPageController {

    private final HomeService homeService;

    @GetMapping("/home")
    public HomeResponseDTO.HomeDTO home(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return homeService.getHomedata(customUserDetails.getUser());
    }

}
