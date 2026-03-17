package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springkong.talki_spring.domain.Practice;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.dto.request.PracticeDTO;
import springkong.talki_spring.enums.PracticeType;
import springkong.talki_spring.exception.NotFoundException;
import springkong.talki_spring.repository.PracticeRepository;
import springkong.talki_spring.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PracticeService {
    private final PracticeRepository practiceRepository;
    private final UserRepository userRepository;

    @Transactional
    public void endPracticeAndSave(User user, PracticeDTO.PracticeDTOBuilder dtoBuilder){
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 입니다."));
        for (PracticeType practiceType:dtoBuilder.getPracticeType()){
            Practice practice = new Practice(persistedUser,
                    practiceType,
                    dtoBuilder.getThoughtRecognition(),
                    dtoBuilder.getBehavioralTestResults(),
                    dtoBuilder.getMindSetting());
            practiceRepository.save(practice);
        }
        persistedUser.updateStreakDays(LocalDate.now());
    }
}
