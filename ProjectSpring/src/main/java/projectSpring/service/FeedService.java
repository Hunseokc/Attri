package projectSpring.service;

import projectSpring.dto.FeedCreateRequestDto;
import projectSpring.dto.FeedResponseDto;
import projectSpring.entity.Bookmark;
import projectSpring.entity.Feed;
import projectSpring.entity.Tag;
import projectSpring.repository.BookmarkRepository;
import projectSpring.repository.FeedRepository;
import projectSpring.repository.TagRepository;
import projectSpring.repository.FeedLikeRepository;
import projectSpring.repository.CommentRepository;
import projectSpring.entity.FeedLike;
import projectSpring.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final TagRepository tagRepository;
    private final BookmarkRepository bookmarkRepository; // 💡 북마크 저장소 연결
    private final FeedLikeRepository feedLikeRepository;
    private final CommentRepository commentRepository;

    // Feed 엔티티 DTO로 변환
    private FeedResponseDto convertToDto(Feed feed) {
        String tagString = feed.getTags().isEmpty() ? "" : "#" + feed.getTags().iterator().next().getName();
        return new FeedResponseDto(
                feed.getId(), feed.getCreator(), feed.getType(), feed.getTitle(), tagString,
                feed.getHeight(), feed.isCollab(), feed.getImageUrl(), feed.getVideoUrl(),
                feed.getContent(), feed.isHideCounts(), feed.isDisableComments()
        );
    }

    // 1. 전체 조회
    public List<FeedResponseDto> getAllFeeds() {
        return feedRepository.findAll().stream()
                .map(this::convertToDto) 
                .collect(Collectors.toList());
    }

    // 2. 카테고리별 조회
    public List<FeedResponseDto> getFeedsByType(String type) {
        return feedRepository.findByType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 3. 북마크 모아보기
    public List<FeedResponseDto> getBookmarkedFeeds(String username) {
        return bookmarkRepository.findAllByUsername(username).stream()
                .map(Bookmark::getFeed) // 영수증에서 원본 피드만
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FeedResponseDto> getUserFeeds(String username) {
        return feedRepository.findByCreatorOrderByIdDesc(username).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FeedResponseDto> getLikedFeeds(String username) {
        return feedLikeRepository.findByUsernameOrderByIdDesc(username).stream()
                .map(FeedLike::getFeed)
                .distinct()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FeedResponseDto> getCommentedFeeds(String username) {
        return commentRepository.findByUsernameOrderByIdDesc(username).stream()
                .map(Comment::getFeed)
                .distinct() // 중복 피드 제거
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 4. 새 피드 저장 메서드
    @Transactional
    public FeedResponseDto createFeed(FeedCreateRequestDto requestDto, MultipartFile file, MultipartFile thumbnail) {
        String imageUrl = null;
        String videoUrl = null;

        // 1. 파일 저장 처리
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String filename = UUID.randomUUID() + "_" + originalFilename;
                Path filePath = Paths.get("uploads", filename);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());

                // 영상인지 이미지인지 판단 (간단하게 확장자나 Content-Type 활용 가능하지만,
                // 여기서는 썸네일(thumbnail)이 같이 오면 file은 영상으로 취급)
                if (thumbnail != null && !thumbnail.isEmpty()) {
                    videoUrl = "/uploads/" + filename; // 원본은 비디오
                    
                    // 썸네일 별도 저장
                    String thumbFilename = UUID.randomUUID() + "_thumb_" + thumbnail.getOriginalFilename();
                    Path thumbFilePath = Paths.get("uploads", thumbFilename);
                    Files.write(thumbFilePath, thumbnail.getBytes());
                    imageUrl = "/uploads/" + thumbFilename; // 이미지 경로에 썸네일 할당
                } else {
                    imageUrl = "/uploads/" + filename; // 일반 이미지 업로드
                }

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패", e);
            }
        }

        // 2. 엔티티 생성 및 데이터 세팅
        Tag tag = tagRepository.findByName(requestDto.getTag())
                .orElseGet(() -> tagRepository.save(new Tag(requestDto.getTag())));

        Feed feed = new Feed();
        
        // title이 없으면 본문 첫 줄이나 빈 문자열 할당
        String title = requestDto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            if (requestDto.getContent() != null && !requestDto.getContent().trim().isEmpty()) {
                title = requestDto.getContent().length() > 20 ? requestDto.getContent().substring(0, 20) + "..." : requestDto.getContent();
            } else {
                title = "Untitled";
            }
        }
        
        feed.setTitle(title);
        feed.setCreator(requestDto.getCreator());
        feed.setType(requestDto.getType());
        feed.setHeight(requestDto.getHeight());
        feed.setCollab(requestDto.isCollab());
        feed.setContent(requestDto.getContent());
        feed.setHideCounts(requestDto.isHideCounts());
        feed.setDisableComments(requestDto.isDisableComments());
        feed.setImageUrl(imageUrl);
        feed.setVideoUrl(videoUrl); // 💡 비디오 경로 DB 세팅!
        feed.getTags().add(tag);

        Feed savedFeed = feedRepository.save(feed);

        return new FeedResponseDto(
                savedFeed.getId(), savedFeed.getCreator(), savedFeed.getType(), savedFeed.getTitle(), "#" + tag.getName(),
                savedFeed.getHeight(), savedFeed.isCollab(), savedFeed.getImageUrl(), savedFeed.getVideoUrl(),
                savedFeed.getContent(), savedFeed.isHideCounts(), savedFeed.isDisableComments()
        );
    }

    // 5. 피드 삭제 메서드
    @Transactional
    public void deleteFeed(Long feedId, String username) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        if (!feed.getCreator().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 연관된 데이터 먼저 삭제 (Cascade 설정이 없으므로 수동 삭제)
        commentRepository.deleteByFeed(feed);
        bookmarkRepository.deleteByFeed(feed);
        feedLikeRepository.deleteByFeed(feed);

        // 태그 매핑 테이블은 Feed 엔티티가 지워질 때 자동 정리되거나 (JPA가 Set 컬렉션을 관리),
        // 필요시 수동으로 비워줄 수 있습니다.
        feed.getTags().clear();

        feedRepository.delete(feed);
    }
}