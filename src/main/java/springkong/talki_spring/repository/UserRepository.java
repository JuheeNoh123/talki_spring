package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String username);
    boolean existsByUserId(String userId);
    Optional<User> findById(Long id);
}
