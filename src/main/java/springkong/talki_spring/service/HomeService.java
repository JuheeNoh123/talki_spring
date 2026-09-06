package springkong.talki_spring.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springkong.talki_spring.domain.Feedback;
import springkong.talki_spring.domain.PracticeSession;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.response.HomeResponseDTO;
import springkong.talki_spring.exception.NotFoundException;
import springkong.talki_spring.repository.FeedbackRepository;
import springkong.talki_spring.repository.PracticeSessionRepository;
import springkong.talki_spring.repository.PresentationRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final PracticeSessionRepository practiceSessionRepository;
    private final PresentationRepository presentationRepository;
    private final FeedbackRepository feedbackRepository;

    public HomeResponseDTO.HomeDTO getHomedata(User user){
        HomeResponseDTO.PracticeDaysDTO practiceDaysDTO = new HomeResponseDTO.PracticeDaysDTO();
        HomeResponseDTO.HomeDTO homeDTO = new HomeResponseDTO.HomeDTO();
        HomeResponseDTO.RecentPresentationReportDTO recentPresentationReportDTO = new HomeResponseDTO.RecentPresentationReportDTO();
        Set<DayOfWeek> practiceDays = new HashSet<>();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY); //이번주 월요일
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY); //이번주 일요일
        LocalDateTime startDateTime = startOfWeek.atStartOfDay(); //localdatetime으로 바꾸기
        LocalDateTime endDateTime = endOfWeek.atTime(23, 59, 59);

        List<PracticeSession> practiceSessions = practiceSessionRepository.findByUserIdAndCreatedAtBetween(user.getId(), startDateTime, endDateTime);
        for (PracticeSession practiceSession : practiceSessions) {
            practiceDays.add(practiceSession.getCreatedAt().getDayOfWeek());
        }
        List<String> result = practiceDays.stream()
                .map(day -> day.name().substring(0, 3)) // MON, TUE
                .toList();

        practiceDaysDTO.setPracticeDays(result);
        practiceDaysDTO.setStreakDays(user.getStreakDays());

        PracticeSession practiceSession = practiceSessionRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElse(null);
        if(practiceSession!=null) homeDTO.setMindSetting(practiceSession.displayThought());
        else homeDTO.setMindSetting(null);

        Presentation presentation =presentationRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElse(null);
        if(presentation!=null) {
            recentPresentationReportDTO.setDateTime(presentation.getCreatedAt());
            recentPresentationReportDTO.setTopic(presentation.getTopic());
            Feedback feedback = feedbackRepository.findByPresentation(presentation)
                    .orElseThrow(() -> new NotFoundException("최근 발표 결과 기록이 없습니다."));
            recentPresentationReportDTO.setTotalScore(feedback.getTotalScore());
        }
        else {
            recentPresentationReportDTO.setTotalScore(0);
            recentPresentationReportDTO.setTopic(null);
            recentPresentationReportDTO.setDateTime(null);
        }

        homeDTO.setUserName(user.getUserName());
        homeDTO.setPracticeDays(practiceDaysDTO);
        homeDTO.setRecentPresentationReport(recentPresentationReportDTO);

        return homeDTO;

    }
}
