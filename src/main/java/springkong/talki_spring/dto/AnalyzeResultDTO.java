package springkong.talki_spring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import springkong.talki_spring.domain.User;

import java.util.Map;

public class AnalyzeResultDTO {
    @Data
    public static class UploadUrlDTO {
        private String filename;
        private Long userId;
        private String presentationType;
    }

    @Data
    public static class ResultDTO {
        @JsonProperty("s3_key")
        private String s3Key;
        @JsonProperty("raw_result")
        private Map<String, Object> rawResult;

        private Map<String, Object> feedback;

    }
}


