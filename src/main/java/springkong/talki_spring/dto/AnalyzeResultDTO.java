package springkong.talki_spring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import springkong.talki_spring.domain.User;

import java.util.Map;
@Getter
public class AnalyzeResultDTO {
    @JsonProperty("s3_key")
    private String s3Key;
    private FeedbackDTO feedback;
    @JsonProperty("raw_result")
    private RawResultDTO rawResult;


    @Data
    public static class UploadUrlDTO {
        private String filename;
        private Long userId;
        private String presentationType;
    }

//    @Data
//    public static class ResultDTO {
//        @JsonProperty("s3_key")
//        private String s3Key;
//        @JsonProperty("raw_result")
//        private Map<String, Object> rawResult;
//
//        private Map<String, Object> feedback;
//
//    }

    @Getter
    public static class FeedbackDTO {
        private Integer score;
        private ScoreDetail score_detail;
        private Metrics metrics;
        private Map<String, Object> llm_feedback;
    }

    @Getter
    public static class ScoreDetail {
        private Double gaze;
        private Double speech_speed;
        private Double pose;
        private Double fillers;
    }

    @Getter
    public static class Metrics {
        private Double speech_wpm;
        private Double pose_warning_ratio;
        private Double gaze_front_ratio;
    }

    @Getter
    public static class RawResultDTO {
        private Map<String, Object> eyes;
        private Map<String, Object> full;
        // 전체 raw를 그냥 Map으로 받음
    }
}


