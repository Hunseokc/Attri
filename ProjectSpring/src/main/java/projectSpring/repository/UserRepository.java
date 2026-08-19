package projectSpring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projectSpring.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // 회원가입 시 중복 아이디/이메일을 걸러내기 헬퍼 메서드
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
