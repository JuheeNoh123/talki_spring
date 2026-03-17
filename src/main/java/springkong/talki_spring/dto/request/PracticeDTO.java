package springkong.talki_spring.dto.request;

import lombok.Data;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.enums.PracticeType;

import java.util.List;

public class PracticeDTO {
    @Data
    public static class PracticeDTOBuilder {
        List<PracticeType> practiceType;
        String thoughtRecognition;
        String behavioralTestResults;
        String mindSetting;
    }
}
