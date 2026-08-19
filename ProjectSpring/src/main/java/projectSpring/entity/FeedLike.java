package projectSpring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_likes")
@Getter @Setter
@NoArgsConstructor
public class FeedLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시물에 좋아요를 눌렀는지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    // 누가 눌렀는지 (현재는 임시로 닉네임 텍스트 저장)
    @Column(nullable = false)
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 편하게 생성하기 위한 생성자
    public FeedLike(Feed feed, String username) {
        this.feed = feed;
        this.username = username;
    }
}
