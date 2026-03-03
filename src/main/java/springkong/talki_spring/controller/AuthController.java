package springkong.talki_spring.controller;

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
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;

    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO.SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/profile/upload-url")
    public ResponseEntity<?> getProfileUploadUrl(@RequestBody UserRequestDTO.ProfileImageRequest request) {
        return ResponseEntity.ok(
                s3Service.generateProfileUploadUrl(request.getFilename())
        );
    }


    @PostMapping("/login")
    public UserResponseDTO.LoginResponse login(@RequestBody UserRequestDTO.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public UserResponseDTO.TokenResponse reissue(@RequestBody UserRequestDTO.TokenRequest request) {
        return authService.reissue(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        String userId = jwtProvider.getUserId(token);
        authService.logout(userId);
    }


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
