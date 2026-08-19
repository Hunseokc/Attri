package projectSpring.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {
    private Long roomId;
    private String otherUsername;
    private String otherNickname;
    private String otherProfileImageUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
}
