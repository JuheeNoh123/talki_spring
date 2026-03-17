package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.dto.response.UserResponseDTO;
import springkong.talki_spring.enums.UserType;
import springkong.talki_spring.exception.DuplicateUserException;
import springkong.talki_spring.exception.InvalidPasswordException;
import springkong.talki_spring.repository.UserRepository;
import springkong.talki_spring.security.JwtProvider;
import springkong.talki_spring.exception.NotFoundException;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;


    public String signup(UserRequestDTO.SignupRequest request) {

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateUserException();
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .userId(request.getUserId())
                .userName(request.getName())
                .password(encodedPassword)
                .email(request.getEmail())
                .userType(UserType.BASIC)
                .streakDays(0)
                .profileImageKey(
                        request.getProfileImageKey() == null
                                ? "profiles/default.png"
                                : request.getProfileImageKey()
                )
                .build();

        userRepository.save(user);

        return "회원가입이 완료되었습니다.";
    }

    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {

        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 틀렸습니다.");
        }

        String access = jwtProvider.createAccessToken(user.getUserId());
        String refresh = jwtProvider.createRefreshToken(user.getUserId());

        // Refresh Redis 저장
        redisTemplate.opsForValue()
                .set("RT:" + user.getUserId(),
                        refresh,
                        7,
                        TimeUnit.DAYS);

        return new UserResponseDTO.LoginResponse(access, refresh);
    }

    public UserResponseDTO.TokenResponse reissue(String refreshToken) {

        jwtProvider.validate(refreshToken);

        String userId = jwtProvider.getUserId(refreshToken);

        String stored = redisTemplate.opsForValue()
                .get("RT:" + userId);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new RuntimeException("Refresh mismatch");
        }

        String newAccess = jwtProvider.createAccessToken(userId);
        String newRefresh = jwtProvider.createRefreshToken(userId);

        redisTemplate.opsForValue()
                .set("RT:" + userId,
                        newRefresh,
                        7,
                        TimeUnit.DAYS);

        return new UserResponseDTO.TokenResponse(newAccess, newRefresh);
    }

    public void logout(String userId) {
        redisTemplate.delete("RT:" + userId);
    }


    public void updateProfileImage(User user, String key) {

        user.updateProfileImage(key);
        userRepository.save(user);
    }

    public void updateProfile(
            User user,
            String userName,
            String userId,
            String email
    ) {

        user.updateProfile(userName, userId, email);

        userRepository.save(user);
    }

    public void changePassword(
            User user,
            String oldPassword,
            String newPassword
    ) {

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호 불일치");
        }

        String encoded = passwordEncoder.encode(newPassword);
        user.changePassword(encoded);

        userRepository.save(user);
    }

    public void updateProfileType(User user, UserType type){
        user.updateUserType(type);
        userRepository.save(user);
    }

    public ResponseEntity<?> checkUserId(String userId) {
        if (userRepository.existsByUserId(userId)) {
            throw new DuplicateUserException();
        }else{
            return null;
        }
    }

    public UserResponseDTO.ProfileResponse getProfile(User user){
        return new UserResponseDTO.ProfileResponse(user.getId(),
                                                    user.getUserName(),
                                                    user.getUserId(),
                                                    user.getEmail(),
                                                    user.getProfileImageKey(),
                                                    user.getUserType());
    }
}
