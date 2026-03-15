package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.dto.response.UserResponseDTO;
import springkong.talki_spring.enums.UserType;
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

    @Operation(summary = "프로필 수정")
    @PatchMapping("/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        authService.updateProfile(
                userDetails.getUser(),
                userDetails.getUserName(),
                userDetails.getEmail()
        );

        return ResponseEntity.ok("프로필 수정 완료");
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/update/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserRequestDTO.ChangePasswordRequest request
    ) {

        authService.changePassword(
                userDetails.getUser(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("비밀번호 변경 완료");
    }

    @Operation(summary = "프로필 이미지 업로드 URL 발급")
    @PostMapping("/upload-url")
    public ResponseEntity<?> getProfileUploadUrl(@RequestBody UserRequestDTO.ProfileImageRequest request) {
        return ResponseEntity.ok(
                s3Service.generateProfileUploadUrl(request.getFilename())
        );
    }


    @Operation(summary = "회원 타입 변경 (ex. basic -> premium)")
    @PutMapping("/update-type")
    public ResponseEntity<?> updateProfileType(@AuthenticationPrincipal CustomUserDetails userDetails, UserType type){
        authService.updateProfileType(userDetails.getUser(), type);
        return ResponseEntity.ok("유저 타입 변경 완료");
    }

    @Operation(summary = "프로필 정보 조회")
    @GetMapping("/get")
    public ResponseEntity<UserResponseDTO.ProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails){
        return  ResponseEntity.ok(authService.getProfile(userDetails.getUser()));
    }
}