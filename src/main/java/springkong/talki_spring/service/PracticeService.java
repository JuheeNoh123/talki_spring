package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springkong.talki_spring.domain.Practice;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.request.PracticeDTO;
import springkong.talki_spring.enums.PracticeType;
import springkong.talki_spring.repository.PracticeRepository;
import springkong.talki_spring.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PracticeService {
    private final PracticeRepository practiceRepository;

    @Transactional
    public void endPracticeAndSave(User user, PracticeDTO.PracticeDTOBuilder dtoBuilder){
        for (PracticeType practiceType:dtoBuilder.getPracticeType()){
            Practice practice = new Practice(user,
                    practiceType,
                    dtoBuilder.getThoughtRecognition(),
                    dtoBuilder.getBehavioralTestResults(),
                    dtoBuilder.getMindSetting());
            practiceRepository.save(practice);
        }
        user.updateStreakDays();
    }
}
