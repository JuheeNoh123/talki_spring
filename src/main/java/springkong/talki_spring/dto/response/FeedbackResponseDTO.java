package springkong.talki_spring.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackResponseDTO {
    @Data
    public static class BasicFeedbackDTO {
        LocalDateTime createdAt;
        String presentationType;
        String userName;
        String s3Key;
        CommonFeedbackResultDTO commonFeedbackResultDTO;
        List<RealTimeResultDTO> realTimeResultDTO;
    }

    @Data
    public static class CommonFeedbackResultDTO {
        private Double fillerScore;
        private Double gazeFrontRatio;
        private Double gazeScore;
        private String llmFeedbackJson;
        private Double poseWarningRatio;
        private Double postureScore;
        private String rawDataJson;
        private Double speechScore;
        private Double speechWpm;
        private Integer totalScore;
        private Double topicScore;
    }

    @Data
    public static class RealTimeResultDTO {
        private Long Id;
        private String type;
        private Double start;
        private Double end;
        private Double duration;
    }
}
