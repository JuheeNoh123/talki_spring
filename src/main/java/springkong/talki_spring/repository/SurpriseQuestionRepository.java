package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.SurpriseQuestion;

import java.util.List;

public interface SurpriseQuestionRepository extends JpaRepository<SurpriseQuestion, Long> {
    List<SurpriseQuestion> findByPresentation(Presentation presentation);
}
