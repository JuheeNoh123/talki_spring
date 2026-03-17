package springkong.talki_spring.dto.request;

import lombok.Data;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.enums.PracticeType;

public class PracticeDTO {
    @Data
    public static class PracticeDTOBuilder {
        PracticeType practiceType;
        String thoughtRecognition;
        String behavioralTestResults;
        String mindSetting;
    }
}
