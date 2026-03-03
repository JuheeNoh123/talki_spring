package springkong.talki_spring.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.dto.response.UserResponseDTO;
import springkong.talki_spring.security.JwtProvider;
import springkong.talki_spring.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO.SignupRequest request) {
        return authService.signup(request);
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
        String username = jwtProvider.getUsername(token);
        authService.logout(username);
    }

}
