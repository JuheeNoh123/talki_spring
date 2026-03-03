package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.request.UserRequestDTO;
import springkong.talki_spring.dto.response.UserResponseDTO;
import springkong.talki_spring.repository.UserRepository;
import springkong.talki_spring.security.JwtProvider;
import springkong.talki_spring.exception.UserNotFoundException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {

        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String access = jwtProvider.createAccessToken(user.getUsername());
        String refresh = jwtProvider.createRefreshToken(user.getUsername());

        // Refresh Redis 저장
        redisTemplate.opsForValue()
                .set("RT:" + user.getUsername(),
                        refresh,
                        7,
                        TimeUnit.DAYS);

        return new UserResponseDTO.LoginResponse(access, refresh);
    }

    public UserResponseDTO.TokenResponse reissue(String refreshToken) {

        jwtProvider.validate(refreshToken);

        String username = jwtProvider.getUsername(refreshToken);

        String stored = redisTemplate.opsForValue()
                .get("RT:" + username);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new RuntimeException("Refresh mismatch");
        }

        String newAccess = jwtProvider.createAccessToken(username);
        String newRefresh = jwtProvider.createRefreshToken(username);

        redisTemplate.opsForValue()
                .set("RT:" + username,
                        newRefresh,
                        7,
                        TimeUnit.DAYS);

        return new UserResponseDTO.TokenResponse(newAccess, newRefresh);
    }

    public void logout(String username) {
        redisTemplate.delete("RT:" + username);
    }
}
