package projectSpring.repository;

import org.springframework.stereotype.Repository;
import projectSpring.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    List<Feed> findByType(String type);
    List<Feed> findByCreatorOrderByIdDesc(String creator);
    long countByCreator(String creator);
}