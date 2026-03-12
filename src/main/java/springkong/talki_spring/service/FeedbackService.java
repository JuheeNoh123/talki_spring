package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import springkong.talki_spring.domain.Feedback;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.RealTimeFeedback;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.request.FeedbackEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import springkong.talki_spring.dto.response.FeedbackResponseDTO;
import springkong.talki_spring.enums.UserType;
import springkong.talki_spring.exception.NotFoundException;
import springkong.talki_spring.repository.FeedbackRepository;
import springkong.talki_spring.repository.PresentationRepository;
import springkong.talki_spring.repository.RealTimeFeedbackRepository;
import springkong.talki_spring.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final PresentationRepository presentationRepository;
    private final RealTimeFeedbackRepository realTimeFeedbackRepository;
    private final ObjectMapper objectMapper;

    public List<FeedbackEventDTO> getRealTimeFeedbacks(String presentationId) {
        String key = "presentation:" + presentationId + ":segments";

        List<String> rawList = redisTemplate.opsForList()
                .range(key, 0, -1);

        if (rawList == null) return List.of();



        return rawList.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, FeedbackEventDTO.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public FeedbackResponseDTO.BasicFeedbackDTO getFeedbacks(Long userId, String presentationId){
        User user = null;
        Presentation presentation = presentationRepository.findById(presentationId).orElseThrow(()->new NotFoundException("PresentationId"));
        Feedback feedback = feedbackRepository.findByPresentation(presentation).orElseThrow(()-> new NotFoundException("Presentation"));
        FeedbackResponseDTO.BasicFeedbackDTO responseDTO = new FeedbackResponseDTO.BasicFeedbackDTO();

        responseDTO.setCreatedAt(feedback.getCreatedAt());
        responseDTO.setPresentationType(presentation.getPresentationType());
        //responseDTO.setUserName(null);
        responseDTO.setS3Key(presentation.getS3Key());

        FeedbackResponseDTO.CommonFeedbackResultDTO commonFeedbackResultDTO = getCommonFeedbackResultDTO(feedback);
        responseDTO.setCommonFeedbackResultDTO(commonFeedbackResultDTO);

        if (userId != null) {
            user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User"));

            responseDTO.setUserName(user.getUserName());
        }

        if (user == null) {
            // 비회원
            presentationRepository.delete(presentation);

            return responseDTO;
        }

        else {
            List<RealTimeFeedback> existing = realTimeFeedbackRepository.findByPresentation(presentation);

            if (existing.isEmpty()) {

                List<FeedbackEventDTO> feedbackEventDTO = getRealTimeFeedbacks(presentationId);
                List<RealTimeFeedback> list = new ArrayList<>();
                for (FeedbackEventDTO dto : feedbackEventDTO) {
                    RealTimeFeedback realTimeFeedback = new RealTimeFeedback();
                    realTimeFeedback.setDuration(dto.getDuration());
                    realTimeFeedback.setPresentation(presentation);
                    realTimeFeedback.setType(dto.getType());
                    realTimeFeedback.setStart(dto.getStart());
                    realTimeFeedback.setEnd(dto.getEnd());
                    list.add(realTimeFeedback);
                }
                realTimeFeedbackRepository.saveAll(list);
            }
            if(user.getUserType() == UserType.BASIC){
                return responseDTO;
            }
            // PREMIUM 회원
            List<RealTimeFeedback> realTimeFeedbackList = realTimeFeedbackRepository.findByPresentation(presentation);
            List<FeedbackResponseDTO.RealTimeResultDTO> realTimeResultDTOList = getRealTimeResultDTOS(realTimeFeedbackList);

            responseDTO.setRealTimeResultDTO(realTimeResultDTOList);
            return responseDTO;
        }

    }

    private static List<FeedbackResponseDTO.RealTimeResultDTO> getRealTimeResultDTOS(List<RealTimeFeedback> realTimeFeedbackList) {
        List<FeedbackResponseDTO.RealTimeResultDTO> realTimeResultDTOList = new ArrayList<>();
        for (RealTimeFeedback realTimeFeedback : realTimeFeedbackList) {
            FeedbackResponseDTO.RealTimeResultDTO resultDTO = new FeedbackResponseDTO.RealTimeResultDTO();
            resultDTO.setId(realTimeFeedback.getId());
            resultDTO.setType(realTimeFeedback.getType());
            resultDTO.setStart(realTimeFeedback.getStart());
            resultDTO.setEnd(realTimeFeedback.getEnd());
            resultDTO.setDuration(realTimeFeedback.getDuration());
            realTimeResultDTOList.add(resultDTO);
        }
        return realTimeResultDTOList;
    }

    private static FeedbackResponseDTO.CommonFeedbackResultDTO getCommonFeedbackResultDTO(Feedback feedback) {
        FeedbackResponseDTO.CommonFeedbackResultDTO commonFeedbackResultDTO = new FeedbackResponseDTO.CommonFeedbackResultDTO();
        commonFeedbackResultDTO.setFillerScore(feedback.getFillerScore());
        commonFeedbackResultDTO.setGazeFrontRatio(feedback.getGazeFrontRatio());
        commonFeedbackResultDTO.setGazeScore(feedback.getGazeScore());
        commonFeedbackResultDTO.setLlmFeedbackJson(feedback.getLlmFeedbackJson());
        commonFeedbackResultDTO.setPoseWarningRatio(feedback.getPoseWarningRatio());
        commonFeedbackResultDTO.setPostureScore(feedback.getPostureScore());
        commonFeedbackResultDTO.setRawDataJson(feedback.getRawDataJson());
        commonFeedbackResultDTO.setSpeechScore(feedback.getSpeechScore());
        commonFeedbackResultDTO.setSpeechWpm(feedback.getSpeechWpm());
        commonFeedbackResultDTO.setTotalScore(feedback.getTotalScore());
        commonFeedbackResultDTO.setTopicScore(feedback.getTopicScore());
        return commonFeedbackResultDTO;
    }
}
