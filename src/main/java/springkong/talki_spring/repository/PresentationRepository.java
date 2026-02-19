package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Presentation;

public interface PresentationRepository extends JpaRepository<Presentation, Long> {

}
