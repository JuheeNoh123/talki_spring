package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.service.AuthService;
import springkong.talki_spring.service.S3Service;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileController {

    private final S3Service s3Service;
    private final AuthService authService;


    @Operation(summary = "프로필 이미지 변경")
    @PostMapping("/image")
    public ResponseEntity<?> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UserRequestDTO.UpdateProfileImageRequest request
    ) {
        authService.updateProfileImage(customUserDetails.getUser(), request.getKey());
        return ResponseEntity.ok("프로필 이미지 업데이트 완료");
    }

    @Operation(summary = "프로필 이미지 다운로드 URL 조회")
    @GetMapping("/image-url")
    public ResponseEntity<?> getProfileImageUrl(@RequestParam String key) {
        return ResponseEntity.ok(
                Map.of("url", s3Service.generateDownloadUrl(key))
        );
    }
}