package springkong.talki_spring.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import springkong.talki_spring.enums.UserType;

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

    @Data
    @AllArgsConstructor
    public static class ProfileResponse{
        private long Id;
        private String userName;
        private String userId;
        private String email;
        private String profileImageKey;
        private UserType userType;
    }
}
