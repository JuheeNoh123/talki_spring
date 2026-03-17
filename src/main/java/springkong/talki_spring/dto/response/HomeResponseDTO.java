package springkong.talki_spring.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class HomeResponseDTO {
    @Data
    public static class HomeDTO {
        private String userName;
        private PracticeDaysDTO practiceDays;
        private RecentPresentationReportDTO recentPresentationReport;

    }

    @Data
    public static class PracticeDaysDTO{
        private int streakDays;
        private List<String> PracticeDays;
    }

    @Data
    public static class RecentPresentationReportDTO{
        private LocalDateTime dateTime;
        private int totalScore;
        private String topic;
    }

    @Data
    public static class presentationHistoryWeekDTO{
        private String practiceDay;
        private LocalDateTime practiceDateTime;
        private int totalScore;
    }

    @Data
    public static class presentationHistoryMonthDTO{
        private String practiceMonth;
        private int avgScore;
    }

    @Data
    public static class presentationHistoryYearDTO{
        private String practiceYear;
        private int avgScore;
    }
}
