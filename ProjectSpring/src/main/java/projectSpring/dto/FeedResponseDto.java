package projectSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedResponseDto {
    private Long id;
    private String creator; // Add creator field
    private String type;
    private String title;
    private String tag;
    private int height;
    private boolean isCollab;
    private String imageUrl;
    private String videoUrl;
    private String content;
    private boolean hideCounts;
    private boolean disableComments;
}
