package projectSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {
    private String username;
    private String nickname;
    private String profileImageUrl;
    private String tag;
    private long postCount;
    private long followerCount;
    private long followingCount;
}
