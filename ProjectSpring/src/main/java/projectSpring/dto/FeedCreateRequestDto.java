package projectSpring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedCreateRequestDto {
    private String title;
    private String creator;
    private String type;     // "music", "art", "video"
    private String tag;      // 예: "Mixing" (프론트에서 입력한 태그)
    private int height;
    private boolean isCollab;
}