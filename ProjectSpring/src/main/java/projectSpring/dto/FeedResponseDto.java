package projectSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedResponseDto {
    private Long id;
    private String type;
    private String title;
    private String tag;     // Vue에서 요구하는 형태 (예: "#Mixing")
    private int height;
    private boolean isCollab;
    private String imageUrl;
}
