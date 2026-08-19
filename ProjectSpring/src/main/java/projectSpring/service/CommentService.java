package projectSpring.service;

import projectSpring.dto.CommentRequestDto;
import projectSpring.dto.CommentResponseDto;
import projectSpring.entity.Comment;
import projectSpring.entity.Feed;
import projectSpring.repository.CommentRepository;
import projectSpring.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;

    // 💡 1. 댓글 작성
    @Transactional
    public CommentResponseDto addComment(Long feedId, CommentRequestDto requestDto) {
        // 1. 어느 게시물에 달린 댓글인지 찾기
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 2. 댓글 엔티티 생성 및 데이터 세팅
        Comment comment = new Comment();
        comment.setFeed(feed);
        comment.setUsername(requestDto.getUsername());
        comment.setContent(requestDto.getContent());

        // 3. DB에 저장
        Comment savedComment = commentRepository.save(comment);

        // 4. 저장된 결과를 DTO로 변환해서 반환
        return new CommentResponseDto(
                savedComment.getId(),
                savedComment.getUsername(),
                savedComment.getContent(),
                savedComment.getCreatedAt()
        );
    }

    // 💡 2. 특정 게시물의 댓글 목록 조회
    public List<CommentResponseDto> getCommentsByFeedId(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // (추후 CommentRepository에 findByFeedOrderByCreatedAtDesc 메서드를 추가하면 더 좋습니다)
        // 현재는 DB에서 직접 연관관계로 가져오거나 전체를 가져와 필터링할 수 있습니다.
        // 가장 간단한 방법으로, 해당 피드에 속한 댓글들을 가져와 DTO로 변환합니다.

        // *주의: 만약 여기서 에러가 난다면 Repository에 메서드를 추가해야 합니다. (아래 팁 참고)
        return commentRepository.findByFeed(feed).stream()
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getUsername(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 💡 3. 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        if (!comment.getUsername().equals(requestDto.getUsername())) {
             throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        comment.setContent(requestDto.getContent());
        Comment savedComment = commentRepository.save(comment);
        
        return new CommentResponseDto(
                savedComment.getId(),
                savedComment.getUsername(),
                savedComment.getContent(),
                savedComment.getCreatedAt()
        );
    }

    // 💡 4. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
                
        if (!comment.getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        commentRepository.delete(comment);
    }
}