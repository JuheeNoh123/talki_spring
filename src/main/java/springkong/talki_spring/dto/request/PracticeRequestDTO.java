package springkong.talki_spring.dto.request;

import lombok.Data;
import springkong.talki_spring.enums.AlternativeThoughtType;
import springkong.talki_spring.enums.AutomaticThoughtType;
import springkong.talki_spring.enums.ExpectationVsRealityType;
import springkong.talki_spring.enums.PracticeMode;

import java.util.List;

public class PracticeRequestDTO {

    @Data
    public static class ThoughtRecognitionRequest {
        private List<AutomaticThoughtType> selectedThoughts;
        private String customThought;
    }

    @Data
    public static class BreathingRequest {
        private boolean completed;
    }

    @Data
    public static class ModeRequest {
        private PracticeMode mode;
    }

    @Data
    public static class BehavioralExperimentRequest {
        private ExpectationVsRealityType expectationVsReality;
    }

    @Data
    public static class AlternativeThoughtRequest {
        private AlternativeThoughtType selectedThought;
        private String customThought;
    }
}
