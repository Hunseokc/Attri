package projectSpring.repository;

import projectSpring.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // 태그 이름으로 검색 (중복 태그 방지용)
    Optional<Tag> findByName(String name);
}