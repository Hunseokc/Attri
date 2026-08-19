package projectSpring.repository;

import projectSpring.entity.Feed;
import projectSpring.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    // 특정 유저가 특정 게시물에 좋아요를 눌렀는지 찾기
    Optional<FeedLike> findByFeedAndUsername(Feed feed, String username);
    void deleteByFeed(Feed feed);

    // 특정 게시물의 총 좋아요 개수
    int countByFeed(Feed feed);

    // 특정 유저가 좋아요한 내역 조회
    java.util.List<FeedLike> findByUsernameOrderByIdDesc(String username);
}