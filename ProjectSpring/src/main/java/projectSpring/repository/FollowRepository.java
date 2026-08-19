package projectSpring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projectSpring.entity.Follow;
import projectSpring.entity.User;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    long countByFollower(User follower); // 내가 팔로우하는 사람 수 (팔로잉)
    long countByFollowing(User following); // 나를 팔로우하는 사람 수 (팔로워)
}
