package projectSpring.controller;

import projectSpring.dto.CommentRequestDto;
import projectSpring.dto.CommentResponseDto;
import projectSpring.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds/{feedId}/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public CommentResponseDto addComment(
            @PathVariable Long feedId,
            @RequestBody CommentRequestDto requestDto) {
        return commentService.addComment(feedId, requestDto);
    }

    // 특정 게시물의 댓글 목록 조회
    @GetMapping
    public List<CommentResponseDto> getComments(@PathVariable Long feedId) {
        return commentService.getCommentsByFeedId(feedId);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public CommentResponseDto updateComment(
            @PathVariable Long feedId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        return commentService.updateComment(commentId, requestDto);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Long feedId,
            @PathVariable Long commentId,
            @RequestParam String username) {
        commentService.deleteComment(commentId, username);
    }
}