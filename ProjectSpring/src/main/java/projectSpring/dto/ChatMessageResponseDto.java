package projectSpring.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private String sender;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
}
