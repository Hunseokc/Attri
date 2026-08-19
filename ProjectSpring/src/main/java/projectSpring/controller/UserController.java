package projectSpring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projectSpring.dto.JwtTokenDto;
import projectSpring.dto.TokenRefreshRequestDto;
import projectSpring.dto.UserLoginRequestDto;
import projectSpring.dto.UserResponseDto;
import projectSpring.dto.UserSignupRequestDto;
import projectSpring.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequestDto requestDto) {
        try {
            UserResponseDto response = userService.signup(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        try {
            JwtTokenDto tokenDto = userService.login(requestDto);
            return ResponseEntity.ok(tokenDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody TokenRefreshRequestDto requestDto) {
        try {
            JwtTokenDto tokenDto = userService.reissue(requestDto.getEmail(), requestDto.getRefreshToken());
            return ResponseEntity.ok(tokenDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않은 사용자입니다.");
        }
        try {
            userService.logout(principal.getName());
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("로그아웃 실패: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않은 사용자입니다.");
        }
        try {
            projectSpring.dto.UserProfileResponseDto profile = userService.getUserProfile(principal.getName());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            java.security.Principal principal,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "profileImage", required = false) org.springframework.web.multipart.MultipartFile profileImage) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않은 사용자입니다.");
        }
        try {
            projectSpring.dto.UserProfileResponseDto updatedProfile = userService.updateUserProfile(principal.getName(), nickname, tag, profileImage);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
