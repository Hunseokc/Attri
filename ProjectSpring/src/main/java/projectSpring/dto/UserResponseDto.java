package projectSpring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectSpring.entity.User;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String username;
    private LocalDateTime createdAt;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.createdAt = user.getCreatedAt();
    }
}
