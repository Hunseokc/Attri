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
    private String tag;
    private int height;
    private boolean isCollab;
    private String content;
    private boolean hideCounts;
    private boolean disableComments;
}