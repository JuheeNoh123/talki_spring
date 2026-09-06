package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.PracticeSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, String> {
    Optional<PracticeSession> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<PracticeSession> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
