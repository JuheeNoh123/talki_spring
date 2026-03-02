package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Presentation;

import java.util.Optional;

public interface PresentationRepository extends JpaRepository<Presentation, Long> {
    Optional<Presentation> findByS3Key(String s3Key);
}
