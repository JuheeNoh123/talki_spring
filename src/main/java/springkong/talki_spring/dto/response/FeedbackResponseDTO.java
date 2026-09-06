package springkong.talki_spring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        List<SurpriseQuestionResultDTO> surpriseQuestions;
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
        private Integer surpriseScore;
    }

    @Data
    public static class RealTimeResultDTO {
        private Long Id;
        private String type;
        private Double start;
        private Double end;
        private Double duration;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;
    }

    @Data
    public static class SurpriseQuestionResultDTO {
        private Long id;
        private String questionId;
        private String question;
        private Double askedAtSeconds;
        private String answerText;
        private Boolean answered;
        private Integer contentScore;
        private Integer gptScore;
        private Integer similarityScore;
        private Integer qualityScore;
        private Integer coherenceScore;
        private String feedback;
    }
}
