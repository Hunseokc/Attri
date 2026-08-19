package projectSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleResponseDto {
    private boolean isToggled; // 토글 상태
    private int totalCount;    // 해당 게시물의 총 좋아요/북마크 개수
}