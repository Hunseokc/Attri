package projectSpring.controller;

import projectSpring.dto.ToggleResponseDto;
import projectSpring.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds") // 공통 주소
public class InteractionController {

    private final InteractionService interactionService;

    // 좋아요
    @PostMapping("/{feedId}/like")
    public ToggleResponseDto toggleLike(
            @PathVariable Long feedId,
            @RequestParam String username) {
        return interactionService.toggleLike(feedId, username);
    }

    // 북마크
    @PostMapping("/{feedId}/bookmark")
    public ToggleResponseDto toggleBookmark(
            @PathVariable Long feedId,
            @RequestParam String username) {
        return interactionService.toggleBookmark(feedId, username);
    }
}