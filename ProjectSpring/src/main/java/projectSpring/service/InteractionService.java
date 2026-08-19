package projectSpring.service;

import projectSpring.dto.ToggleResponseDto;
import projectSpring.entity.Bookmark;
import projectSpring.entity.Feed;
import projectSpring.entity.FeedLike;
import projectSpring.repository.BookmarkRepository;
import projectSpring.repository.FeedLikeRepository;
import projectSpring.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final BookmarkRepository bookmarkRepository;

    // 좋아요 토글 (있으면 삭제, 없으면 추가)
    @Transactional
    public ToggleResponseDto toggleLike(Long feedId, String username) {
        // 1. 게시물 찾기 (없으면 에러 발생)
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("해당 피드를 찾을 수 없습니다."));

        // 2. 이 유저가 이 게시물에 좋아요를 누른 기록 있는지 확인
        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUsername(feed, username);
        boolean isToggled;

        if (existingLike.isPresent()) {
            // 이미 기록 있다면? -> 좋아요 취소 (삭제)
            feedLikeRepository.delete(existingLike.get());
            isToggled = false;
        } else {
            // 기록 없다면? -> 좋아요 추가 (저장)
            feedLikeRepository.save(new FeedLike(feed, username));
            isToggled = true;
        }

        // 3. 현재 게시물의 총 좋아요 개수 다시 세기
        int totalCount = feedLikeRepository.countByFeed(feed);

        // 4. 프론트엔드에 결과 반환
        return new ToggleResponseDto(isToggled, totalCount);
    }

    // 북마크 토글 (좋아요와 로직 동일, 대상 테이블만 다르게)
    @Transactional
    public ToggleResponseDto toggleBookmark(Long feedId, String username) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("해당 피드를 찾을 수 없습니다."));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByFeedAndUsername(feed, username);
        boolean isToggled;

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            isToggled = false;
        } else {
            bookmarkRepository.save(new Bookmark(feed, username));
            isToggled = true;
        }

        int totalCount = bookmarkRepository.countByFeed(feed);
        return new ToggleResponseDto(isToggled, totalCount);
    }
}