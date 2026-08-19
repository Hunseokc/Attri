package projectSpring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projectSpring.entity.ChatMessage;
import projectSpring.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방의 메시지 목록 (시간순)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // 채팅방의 마지막 메시지
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 읽지 않은 메시지 수 (상대방이 보낸 것 중 아직 안 읽은 것)
    long countByChatRoomAndSenderNotAndReadAtIsNull(ChatRoom chatRoom, String myUsername);

    // 읽음 처리: 상대방이 보낸 읽지 않은 메시지를 일괄 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage m SET m.readAt = CURRENT_TIMESTAMP WHERE m.chatRoom = :room AND m.sender <> :myUsername AND m.readAt IS NULL")
    void markAsRead(@Param("room") ChatRoom room, @Param("myUsername") String myUsername);
}
