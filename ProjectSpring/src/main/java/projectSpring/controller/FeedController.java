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

    // 목록 조회 API
    @GetMapping("/api/feeds")
    public List<FeedResponseDto> getFeeds() {
        return feedService.getAllFeeds();
    }

    // 새 피드 작성 API
    @PostMapping(value = "/api/feeds", consumes = "multipart/form-data")
    public FeedResponseDto createFeed(
            @ModelAttribute FeedCreateRequestDto requestDto,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return feedService.createFeed(requestDto, file);
    }
}