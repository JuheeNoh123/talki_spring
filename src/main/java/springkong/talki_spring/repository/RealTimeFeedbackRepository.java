package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.RealTimeFeedback;

import java.util.List;

public interface RealTimeFeedbackRepository extends JpaRepository<RealTimeFeedback, Long> {
    List<RealTimeFeedback> findByPresentation(Presentation presentation);
}
