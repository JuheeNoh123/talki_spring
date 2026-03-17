package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.Practice;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeRepository extends JpaRepository<Practice, Long> {

    List<Practice> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
