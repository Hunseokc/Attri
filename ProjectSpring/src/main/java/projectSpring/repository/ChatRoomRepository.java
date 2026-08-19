package projectSpring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projectSpring.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 두 유저 간 기존 채팅방 조회 (순서 무관)
    @Query("SELECT r FROM ChatRoom r WHERE (r.user1 = :u1 AND r.user2 = :u2) OR (r.user1 = :u2 AND r.user2 = :u1)")
    Optional<ChatRoom> findByUsers(@Param("u1") String u1, @Param("u2") String u2);

    // 내가 속한 모든 채팅방
    @Query("SELECT r FROM ChatRoom r WHERE r.user1 = :username OR r.user2 = :username")
    List<ChatRoom> findAllByUsername(@Param("username") String username);
}
