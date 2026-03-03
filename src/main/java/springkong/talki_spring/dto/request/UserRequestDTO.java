package springkong.talki_spring.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequestDTO {
    @Data
    public static class SignupRequest{
        @NotBlank(message = "아이디는 필수입니다.")
        private String userId;
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
//        @NotBlank(message = "이름은 필수입니다.")
//        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class LoginRequest{
        @NotBlank(message = "아이디는 필수입니다.")
        private String userId;
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class TokenRequest {
        private String refreshToken;
    }
}
