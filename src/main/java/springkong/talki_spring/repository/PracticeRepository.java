package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Practice;
import springkong.talki_spring.domain.Presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PracticeRepository extends JpaRepository<Practice, Long> {
    Optional<Practice> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<Practice> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
