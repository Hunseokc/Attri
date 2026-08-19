package projectSpring.controller;

import projectSpring.dto.FeedCreateRequestDto;
import projectSpring.dto.FeedResponseDto;
import projectSpring.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/api/feeds")
    public List<FeedResponseDto> getFeeds(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return feedService.getFeedsByType(type);
        }
        return feedService.getAllFeeds();
    }

    // 새 피드 작성 API
    @PostMapping(value = "/api/feeds", consumes = "multipart/form-data")
    public FeedResponseDto createFeed(
            @ModelAttribute FeedCreateRequestDto requestDto,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        return feedService.createFeed(requestDto, file, thumbnail);
    }

    @GetMapping("/api/feeds/bookmarks")
    public List<FeedResponseDto> getBookmarkedFeeds(@RequestParam String username) {
        return feedService.getBookmarkedFeeds(username);
    }

    @GetMapping("/api/feeds/user")
    public List<FeedResponseDto> getUserFeeds(@RequestParam String username) {
        return feedService.getUserFeeds(username);
    }

    @GetMapping("/api/feeds/liked")
    public List<FeedResponseDto> getLikedFeeds(@RequestParam String username) {
        return feedService.getLikedFeeds(username);
    }

    @GetMapping("/api/feeds/commented")
    public List<FeedResponseDto> getCommentedFeeds(@RequestParam String username) {
        return feedService.getCommentedFeeds(username);
    }

    // 피드 삭제 API
    @DeleteMapping("/api/feeds/{id}")
    public void deleteFeed(@PathVariable Long id, @RequestParam String username) {
        feedService.deleteFeed(id, username);
    }
}