package projectSpring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSpring.dto.JwtTokenDto;
import projectSpring.dto.UserLoginRequestDto;
import projectSpring.dto.UserResponseDto;
import projectSpring.dto.UserSignupRequestDto;
import projectSpring.entity.User;
import projectSpring.repository.UserRepository;
import projectSpring.repository.FeedRepository;
import projectSpring.security.JwtTokenProvider;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import projectSpring.dto.UserProfileResponseDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public UserResponseDto signup(UserSignupRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 사용자 이름입니다.");
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setUsername(requestDto.getUsername());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public JwtTokenDto login(UserLoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 인증 성공 후 토큰 발급 (이메일을 principal 로 사용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), "", Collections.emptyList());
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Redis에 Refresh Token 저장 (키: RT:{email}, 값: refreshToken, 만료시간 설정)
        // 만료시간은 JwtTokenProvider 의 refreshExpirationTime 과 동일하게 가져오거나 임의로 7일 설정. 
        // 편의상 7일(604800000ms = 7일)로 세팅
        redisTemplate.opsForValue().set("RT:" + user.getEmail(), refreshToken, 7, TimeUnit.DAYS);

        return new JwtTokenDto("Bearer", accessToken, refreshToken);
    }

    // Access Token 재발급 로직
    @Transactional(readOnly = true)
    public JwtTokenDto reissue(String email, String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Redis 에서 사용자 이메일 기반으로 저장된 Refresh Token 값을 가져옴
        String redisRefreshToken = redisTemplate.opsForValue().get("RT:" + email);
        if (redisRefreshToken == null || !redisRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("저장된 Refresh Token과 일치하지 않습니다.");
        }

        // 3. 새로운 Access Token 발급
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "", Collections.emptyList());
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        return new JwtTokenDto("Bearer", newAccessToken, refreshToken);
    }

    // Access Token을 기반으로 로그아웃 처리
    @Transactional
    public void logout(String email) {
        // Redis에서 사용자 이메일 기반으로 저장된 Refresh Token 삭제
        if (Boolean.TRUE.equals(redisTemplate.hasKey("RT:" + email))) {
            redisTemplate.delete("RT:" + email);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // TODO: follower count, following count are mocked for now
        // since we don't have Follow features yet.
        long postCount = feedRepository.countByCreator(user.getUsername());
        
        return UserProfileResponseDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .tag(user.getTag())
                .postCount(postCount)
                .followerCount(0)
                .followingCount(0)
                .build();
    }

    @Transactional
    public UserProfileResponseDto updateUserProfile(String email, String nickname, String tag, MultipartFile profileImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (nickname != null) {
            user.setNickname(nickname);
        }

        if (tag != null) {
            user.setTag(tag);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String filename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
                Path filePath = Paths.get("uploads", filename);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, profileImage.getBytes());
                user.setProfileImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Profile image save failed", e);
            }
        }
        
        return getUserProfile(email);
    }
}
