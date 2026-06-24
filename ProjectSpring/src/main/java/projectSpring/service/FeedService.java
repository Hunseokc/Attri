package projectSpring.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import projectSpring.dto.FeedCreateRequestDto;
import projectSpring.dto.FeedResponseDto;
import projectSpring.entity.Feed;
import projectSpring.entity.Tag;
import projectSpring.repository.FeedRepository;
import projectSpring.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final TagRepository tagRepository; // Tag 관리를 위해 추가됨

    // 모든 피드를 일반 목록으로 (추후 최신순 등 정렬 추가)
    public List<FeedResponseDto> getAllFeeds() {
        List<Feed> feeds = feedRepository.findAll();
        return feeds.stream().map(feed -> {
            String tagString = feed.getTags().isEmpty() ? "" : "#" + feed.getTags().iterator().next().getName();
            return new FeedResponseDto(
                    feed.getId(), feed.getType(), feed.getTitle(), tagString, feed.getHeight(), feed.isCollab(),
                    feed.getImageUrl() // 💡 추가됨
            );
        }).collect(Collectors.toList());
    }

    // 새 피드 저장 메서드
    @Transactional
    public FeedResponseDto createFeed(FeedCreateRequestDto requestDto, MultipartFile file) {

        // 1. 파일 저장 로직
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                // 원본 이름이 겹치지 않게 UUID(랜덤문자열) 부착
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get("uploads", filename);
                Files.createDirectories(filePath.getParent()); // uploads 폴더가 없으면 자동 생성
                Files.write(filePath, file.getBytes()); // 디스크에 저장
                imageUrl = "/uploads/" + filename; // DB에 저장될 접근 URL
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패", e);
            }
        }

        // 2. 엔티티 생성 및 데이터 세팅
        Tag tag = tagRepository.findByName(requestDto.getTag())
                .orElseGet(() -> tagRepository.save(new Tag(requestDto.getTag())));

        Feed feed = new Feed();
        feed.setTitle(requestDto.getTitle());
        feed.setCreator(requestDto.getCreator());
        feed.setType(requestDto.getType());
        feed.setHeight(requestDto.getHeight());
        feed.setCollab(requestDto.isCollab());
        feed.setImageUrl(imageUrl); // 💡 이미지 경로 DB 세팅!
        feed.getTags().add(tag);

        Feed savedFeed = feedRepository.save(feed);

        return new FeedResponseDto(
                savedFeed.getId(), savedFeed.getType(), savedFeed.getTitle(), "#" + tag.getName(),
                savedFeed.getHeight(), savedFeed.isCollab(), savedFeed.getImageUrl() // 💡 반환 DTO에도 추가
        );
    }
}