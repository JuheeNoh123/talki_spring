package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.PracticeSession;
import springkong.talki_spring.domain.PracticeSubStepResult;

import java.util.List;

public interface PracticeSubStepResultRepository extends JpaRepository<PracticeSubStepResult, Long> {
    List<PracticeSubStepResult> findByPracticeSession(PracticeSession practiceSession);
}
