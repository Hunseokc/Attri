package projectSpring.repository;

import org.springframework.stereotype.Repository;
import projectSpring.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projectSpring.entity.User;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    List<Feed> findByType(String type);
    List<Feed> findByCreatorOrderByIdDesc(String creator);
    long countByCreator(String creator);

    @Query("SELECT f FROM Feed f JOIN Follow fw ON f.creator = fw.following.username WHERE fw.follower = :follower ORDER BY f.id DESC")
    List<Feed> findFollowingFeeds(@Param("follower") User follower);
}