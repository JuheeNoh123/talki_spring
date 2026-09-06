package springkong.talki_spring.dto.response;

import lombok.Data;
import springkong.talki_spring.enums.AlternativeThoughtType;
import springkong.talki_spring.enums.AutomaticThoughtType;
import springkong.talki_spring.enums.ExpectationVsRealityType;
import springkong.talki_spring.enums.PracticeMode;
import springkong.talki_spring.enums.PracticeStep;
import springkong.talki_spring.enums.PracticeType;

import java.time.LocalDateTime;
import java.util.List;

public class PracticeResponseDTO {

    @Data
    public static class SessionStartResponse {
        private String sessionId;
        private PracticeStep currentStep;
    }

    @Data
    public static class StepResponse {
        private PracticeStep currentStep;
        private List<PracticeType> subStepQueue;
    }

    @Data
    public static class SessionStateResponse {
        private PracticeStep currentStep;
        private PracticeMode mode;
        private List<PracticeType> subStepQueue;
        private List<PracticeType> completedSubSteps;
    }

    @Data
    public static class CompleteResponse {
        private String sessionId;
        private PracticeMode mode;
        private List<PracticeType> completedSubSteps;
        private String alternativeThought;
    }

    @Data
    public static class ReportResponse {
        private String sessionId;
        private LocalDateTime createdAt;
        private PracticeMode mode;
        private ThoughtRecognitionResult thoughtRecognition;
        private BehavioralExperimentResult behavioralExperiment;
        private AlternativeThoughtResult alternativeThought;
        private List<SubStepResultResponse> subStepResults;
    }

    @Data
    public static class ThoughtRecognitionResult {
        private List<AutomaticThoughtType> selectedThoughts;
        private String customThought;
    }

    @Data
    public static class BehavioralExperimentResult {
        private ExpectationVsRealityType expectationVsReality;
    }

    @Data
    public static class AlternativeThoughtResult {
        private AlternativeThoughtType selectedThought;
        private String customThought;
    }

    @Data
    public static class SubStepResultResponse {
        private PracticeType subStep;
        private Integer score;
        private String feedbackText;
        private String rawResultJson;
    }
}
