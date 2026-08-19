package projectSpring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import projectSpring.dto.ChatMessageRequestDto;
import projectSpring.dto.ChatMessageResponseDto;
import projectSpring.dto.ChatRoomResponseDto;
import projectSpring.entity.User;
import projectSpring.repository.UserRepository;
import projectSpring.service.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    /**
     * 채팅방 생성 또는 조회
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDto> createOrGetRoom(
            @RequestParam String otherUsername,
            Authentication authentication) {
        String myEmail = authentication.getName();
        String myUsername = getUsername(myEmail);
        return ResponseEntity.ok(chatService.getOrCreateChatRoom(myUsername, otherUsername));
    }

    /**
     * 내 채팅방 목록
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getChatRooms(Authentication authentication) {
        String myEmail = authentication.getName();
        String myUsername = getUsername(myEmail);
        return ResponseEntity.ok(chatService.getChatRooms(myUsername));
    }

    /**
     * 메시지 목록 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long roomId,
            Authentication authentication) {
        String myEmail = authentication.getName();
        String myUsername = getUsername(myEmail);
        return ResponseEntity.ok(chatService.getMessages(roomId, myUsername));
    }

    /**
     * 메시지 전송
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequestDto request,
            Authentication authentication) {
        String myEmail = authentication.getName();
        String myUsername = getUsername(myEmail);
        return ResponseEntity.ok(chatService.sendMessage(roomId, myUsername, request.getContent()));
    }

    /**
     * 유저 검색 (채팅 대상 찾기)
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(
            @RequestParam String keyword,
            Authentication authentication) {
        String myEmail = authentication.getName();
        String myUsername = getUsername(myEmail);

        List<UserSearchResult> results = userRepository.findByUsernameContainingIgnoreCase(keyword).stream()
                .filter(u -> !u.getUsername().equals(myUsername)) // 자기 자신 제외
                .limit(10)
                .map(u -> new UserSearchResult(
                    u.getUsername(),
                    u.getNickname() != null ? u.getNickname() : u.getUsername(),
                    u.getProfileImageUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // JWT subject는 email이므로 username 변환 필요
    private String getUsername(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 검색 결과용 간단 DTO
    public record UserSearchResult(String username, String nickname, String profileImageUrl) {}
}
