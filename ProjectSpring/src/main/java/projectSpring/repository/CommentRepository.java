package projectSpring.repository;

import projectSpring.entity.Comment;
import projectSpring.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 모든 댓글
    List<Comment> findByFeed(Feed feed);

    // 특정 유저가 작성한 댓글 조회
    List<Comment> findByUsernameOrderByIdDesc(String username);

    void deleteByFeed(Feed feed);
}
