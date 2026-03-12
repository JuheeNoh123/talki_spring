package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import springkong.talki_spring.dto.request.FeedbackEventDTO;
//import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final RedisTemplate<String, String> redisTemplate;

    public List<FeedbackEventDTO> getFeedbacks(String presentationId) {
        String key = "presentation:" + presentationId + ":segments";

        List<String> rawList = redisTemplate.opsForList()
                .range(key, 0, -1);

        if (rawList == null) return List.of();

        ObjectMapper mapper = new ObjectMapper();

        return rawList.stream()
                .map(json -> {
                    try {
                        return mapper.readValue(json, FeedbackEventDTO.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
