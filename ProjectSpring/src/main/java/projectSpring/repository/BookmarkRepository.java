package projectSpring.repository;

import projectSpring.entity.Bookmark;
import projectSpring.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // 특정 유저가 특정 게시물에 북마크를 눌렀는지
    Optional<Bookmark> findByFeedAndUsername(Feed feed, String username);

    int countByFeed(Feed feed);

    // 특정 유저가 저장한 모든 북마크 찾아오기
    List<Bookmark> findAllByUsername(String username);
    void deleteByFeed(Feed feed);
}