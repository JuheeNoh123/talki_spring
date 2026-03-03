package springkong.talki_spring.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

public class UserResponseDTO {
    @Data
    @AllArgsConstructor
    public static class LoginResponse{
        private String access;
        private String refresh;
    }

    @Data
    @AllArgsConstructor
    public static class TokenResponse{
        private String newAccess;
        private String newRefresh;
    }
}
