package springkong.talki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springkong.talki_spring.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
