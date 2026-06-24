package projectSpring.repository;

import org.springframework.stereotype.Repository;
import projectSpring.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 기본적으로 제공되는 findAll(), save(), findById() 등을 그대로 사용합니다.
}