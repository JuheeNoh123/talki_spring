package springkong.talki_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import springkong.talki_spring.enums.AlternativeThoughtType;
import springkong.talki_spring.enums.AutomaticThoughtType;
import springkong.talki_spring.enums.ExpectationVsRealityType;
import springkong.talki_spring.enums.PracticeMode;
import springkong.talki_spring.enums.PracticeStep;
import springkong.talki_spring.enums.PracticeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis에 JSON으로 저장되는 연습 세션 진행 상태.
 * 세션 재개를 지원하지 않으므로 MySQL이 아닌 Redis(TTL)에만 존재한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSessionStateDTO {

    private Long userId;
    private PracticeStep currentStep;
    private PracticeMode mode;
    private List<PracticeType> subStepQueue = new ArrayList<>();
    private List<PracticeType> completedSubSteps = new ArrayList<>();

    private List<AutomaticThoughtType> selectedThoughts;
    private String customThought;

    private Boolean breathingCompleted;

    private List<SubStepResult> subStepResults = new ArrayList<>();

    private ExpectationVsRealityType expectationVsReality;

    private AlternativeThoughtType alternativeThought;
    private String alternativeThoughtCustom;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubStepResult {
        private PracticeType subStep;
        private Integer score;
        private String feedbackText;
        private String rawResultJson;
    }
}
