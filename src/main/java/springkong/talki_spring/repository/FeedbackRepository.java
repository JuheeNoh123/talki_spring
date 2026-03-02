package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Feedback;
import springkong.talki_spring.domain.Presentation;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByPresentation(Presentation presentation);
}
