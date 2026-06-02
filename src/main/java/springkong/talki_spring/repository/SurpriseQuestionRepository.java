package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.SurpriseQuestion;

public interface SurpriseQuestionRepository extends JpaRepository<SurpriseQuestion, Long> {
}
