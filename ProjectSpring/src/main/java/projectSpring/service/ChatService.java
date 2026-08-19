package projectSpring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSpring.dto.ChatMessageResponseDto;
import projectSpring.dto.ChatRoomResponseDto;
import projectSpring.entity.ChatMessage;
import projectSpring.entity.ChatRoom;
import projectSpring.entity.User;
import projectSpring.repository.ChatMessageRepository;
import projectSpring.repository.ChatRoomRepository;
import projectSpring.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 채팅방 생성 또는 기존 방 반환
     */
    @Transactional
    public ChatRoomResponseDto getOrCreateChatRoom(String myUsername, String otherUsername) {
        if (myUsername.equals(otherUsername)) {
            throw new IllegalArgumentException("자기 자신에게 메시지를 보낼 수 없습니다.");
        }

        ChatRoom room = chatRoomRepository.findByUsers(myUsername, otherUsername)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setUser1(myUsername);
                    newRoom.setUser2(otherUsername);
                    return chatRoomRepository.save(newRoom);
                });

        return toChatRoomDto(room, myUsername);
    }

    /**
     * 내 채팅방 목록 (최근 메시지순)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getChatRooms(String myUsername) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByUsername(myUsername);

        return rooms.stream()
                .map(room -> toChatRoomDto(room, myUsername))
                .sorted(Comparator.comparing(
                    (ChatRoomResponseDto dto) -> dto.getLastMessageTime(),
                    Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 메시지 목록 조회 + 읽음 처리
     */
    @Transactional
    public List<ChatMessageResponseDto> getMessages(Long roomId, String myUsername) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 권한 확인
        if (!room.getUser1().equals(myUsername) && !room.getUser2().equals(myUsername)) {
            throw new RuntimeException("이 채팅방에 접근할 권한이 없습니다.");
        }

        // 읽음 처리
        chatMessageRepository.markAsRead(room, myUsername);

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room).stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(Long roomId, String sender, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        if (!room.getUser1().equals(sender) && !room.getUser2().equals(sender)) {
            throw new RuntimeException("이 채팅방에 접근할 권한이 없습니다.");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setContent(content);
        chatMessageRepository.save(message);

        return toMessageDto(message);
    }

    // --- 변환 헬퍼 ---

    private ChatRoomResponseDto toChatRoomDto(ChatRoom room, String myUsername) {
        String otherUsername = room.getUser1().equals(myUsername) ? room.getUser2() : room.getUser1();

        User otherUser = userRepository.findByUsername(otherUsername).orElse(null);
        String otherNickname = otherUser != null ? otherUser.getNickname() : otherUsername;
        String otherProfileImageUrl = otherUser != null ? otherUser.getProfileImageUrl() : null;

        var lastMsg = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(room);
        long unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndReadAtIsNull(room, myUsername);

        return ChatRoomResponseDto.builder()
                .roomId(room.getId())
                .otherUsername(otherUsername)
                .otherNickname(otherNickname != null ? otherNickname : otherUsername)
                .otherProfileImageUrl(otherProfileImageUrl)
                .lastMessage(lastMsg.map(ChatMessage::getContent).orElse(null))
                .lastMessageTime(lastMsg.map(ChatMessage::getCreatedAt).orElse(room.getCreatedAt()))
                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageResponseDto toMessageDto(ChatMessage msg) {
        return ChatMessageResponseDto.builder()
                .id(msg.getId())
                .sender(msg.getSender())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .isRead(msg.getReadAt() != null)
                .build();
    }
}
