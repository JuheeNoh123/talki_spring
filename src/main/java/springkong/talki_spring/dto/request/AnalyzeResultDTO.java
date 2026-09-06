package springkong.talki_spring.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
public class AnalyzeResultDTO {
    @JsonProperty("s3_key")
    private String s3Key;

    @JsonProperty("raw_data")
    private RawResultDTO rawData;

    private ScoresDTO scores;

    @JsonProperty("llm_feedback")
    private Map<String, String> llmFeedback;

    @JsonProperty("surprise_questions")
    private List<SurpriseQuestionDTO> surpriseQuestions = new ArrayList<>();

    // ===== 다른 API에서 사용하는 내부 클래스 (유지) =====

    @Data
    public static class UploadUrlDTO {
        private String presentationId;
        private String filename;
        private Long userId;
        private String presentationType;
        private String topic;
    }

    @Data
    public static class TopicDTO {
        private String topic_summary;
        private String topic_desc;
        private List<String> topic_tags;
    }

    // ===== scores =====

    @Getter
    @ToString
    public static class ScoresDTO {
        @JsonProperty("total_score")
        private Integer totalScore;

        @JsonProperty("score_detail")
        private ScoreDetail scoreDetail;
    }

    @Getter
    @ToString
    public static class ScoreDetail {
        private Integer gaze;
        @JsonProperty("speech_speed")
        private Integer speechSpeed;
        private Integer fillers;
        private Integer pose;
        private Integer topic;
        @JsonProperty("topic_relevance")
        private Integer topicRelevance;
        @JsonProperty("topic_quality")
        private Integer topicQuality;
        private Integer surprise;
    }

    @Getter
    @ToString
    public static class SurpriseQuestionDTO {
        @JsonProperty("question_id")
        private String questionId;
        private String question;
        @JsonProperty("asked_at_seconds")
        private Double askedAtSeconds;
        @JsonProperty("answer_text")
        private String answerText;
        private Boolean answered;
        @JsonProperty("content_score")
        private Integer contentScore;
        @JsonProperty("gpt_score")
        private Integer gptScore;
        @JsonProperty("similarity_score")
        private Integer similarityScore;
        @JsonProperty("quality_score")
        private Integer qualityScore;
        @JsonProperty("coherence_score")
        private Integer coherenceScore;
        private String feedback;
    }

    // ===== raw_data =====

    @Getter
    @ToString
    public static class RawResultDTO {
        private SpeechDTO speech;
        private PoseDTO pose;
        private GazeDTO gaze;
        private TopicResultDTO topic;
    }

    @Getter
    @ToString
    public static class SpeechDTO {
        private Double wpm;
        @JsonProperty("fillers_count")
        private Integer fillersCount;
        @JsonProperty("fillers_freq")
        private Double fillersFreq;
        @JsonProperty("silence_count")
        private Integer silenceCount;
        @JsonProperty("total_silence_sec")
        private Double totalSilenceSec;
        @JsonProperty("silence_ratio")
        private Double silenceRatio;
        private String text;
    }

    @Getter
    @ToString
    public static class PoseDTO {
        @JsonProperty("avg_speed")
        private Double avgSpeed;
        @JsonProperty("max_speed")
        private Double maxSpeed;
        @JsonProperty("warning_count")
        private Integer warningCount;
        @JsonProperty("warning_ratio")
        private Double warningRatio;
        @JsonProperty("rigid_count")
        private Integer rigidCount;
        @JsonProperty("rigid_ratio")
        private Double rigidRatio;
        private Integer samples;
    }

    @Getter
    @ToString
    public static class GazeDTO {
        @JsonProperty("avg_dx")
        private Double avgDx;
        @JsonProperty("avg_dy")
        private Double avgDy;
        @JsonProperty("horizontal_mode")
        private String horizontalMode;
        @JsonProperty("vertical_mode")
        private String verticalMode;
        @JsonProperty("horizontal_counts")
        private Map<String, Integer> horizontalCounts;
        @JsonProperty("vertical_counts")
        private Map<String, Integer> verticalCounts;
        private Integer samples;
    }

    @Getter
    @ToString
    public static class TopicResultDTO {
        @JsonProperty("on_topic")
        private Boolean onTopic;
        private TopicScoresDTO scores;
        private List<String> keywords;
        private EvidenceDTO evidence;
        @JsonProperty("sentence_analysis")
        private List<SentenceAnalysisDTO> sentenceAnalysis;
        @JsonProperty("worst_sentence")
        private SentenceDTO worstSentence;
    }

    @Getter
    @ToString
    public static class TopicScoresDTO {
        @JsonProperty("final")
        private Integer finalScore;
        private Integer topic;
        private Integer quality;
    }

    @Getter
    @ToString
    public static class EvidenceDTO {
        @JsonProperty("on_topic_sentences")
        private List<String> onTopicSentences;
        @JsonProperty("off_topic_sentences")
        private List<String> offTopicSentences;
    }

    @Getter
    @ToString
    public static class SentenceAnalysisDTO {
        private String sentence;
        @JsonProperty("topic_score")
        private Integer topicScore;
        @JsonProperty("coherence_sim")
        private Double coherenceSim;
        @JsonProperty("quality_score")
        private Integer qualityScore;
        private List<String> flags;
    }

    @Getter
    @ToString
    public static class SentenceDTO {
        private String sentence;
        @JsonProperty("topic_score")
        private Integer topicScore;
        private List<String> flags;
    }
}
