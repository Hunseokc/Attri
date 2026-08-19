package projectSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private boolean isFollowing;
    
    @JsonProperty("isAdmin")
    private boolean isAdmin;
}
