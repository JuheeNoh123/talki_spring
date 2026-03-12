package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.dto.response.UserResponseDTO;
import springkong.talki_spring.security.CustomUserDetails;
import springkong.talki_spring.security.JwtProvider;
import springkong.talki_spring.service.AuthService;
import springkong.talki_spring.service.S3Service;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원 인증 API")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;

    @Operation(summary = "회원가입", description = "새로운 사용자를 생성합니다")
    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO.SignupRequest request) {
        return authService.signup(request);
    }

    @Operation(summary = "프로필 이미지 업로드 URL 발급")
    @PostMapping("/profile/upload-url")
    public ResponseEntity<?> getProfileUploadUrl(@RequestBody UserRequestDTO.ProfileImageRequest request) {
        return ResponseEntity.ok(
                s3Service.generateProfileUploadUrl(request.getFilename())
        );
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public UserResponseDTO.LoginResponse login(@RequestBody UserRequestDTO.LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public UserResponseDTO.TokenResponse reissue(@RequestBody UserRequestDTO.TokenRequest request) {
        return authService.reissue(request.getRefreshToken());
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        String userId = jwtProvider.getUserId(token);
        authService.logout(userId);
    }


    @Operation(summary = "프로필 수정")
    @PatchMapping("/profile/update")
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
    @PatchMapping("/profile/update/password")
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
}
