package projectSpring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "feeds")
@Getter @Setter
@NoArgsConstructor
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 창작물 제목 또는 설명

    @Column(nullable = false)
    private String creator; // 작성자 이름 (추후 User 엔티티와 연관관계 매핑)

    @Column(nullable = false)
    private String type; // 미디어 타입: "music", "art", "video"

    @Column(nullable = false)
    private int height; // 핀터레스트 느낌 레이아웃용 고유 높이 값 (250, 350)

    @Column(name = "is_collab", nullable = false)
    private boolean isCollab; // 협업 모집 여부 true/false (지금은 이모티콘 🤝 으로 간소화)

    @Column(columnDefinition = "TEXT")
    private String content; // 본문 글 (선택적)

    @Column(name = "hide_counts", columnDefinition = "boolean default false")
    private boolean hideCounts = false; // 좋아요/조회수 숨기기

    @Column(name = "disable_comments", columnDefinition = "boolean default false")
    private boolean disableComments = false; // 댓글 해제

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl; // 이미지(썸네일) 경로 저장

    @Column(name = "video_url")
    private String videoUrl; // 비디오 경로 저장 (비디오일 경우)

    // BPM, 아트 해상도 등 타입별 데이터는 스키마 변경 없이 JSON으로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    // 태그와의 N:N 관계 설정
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "feed_tags",
            joinColumns = @JoinColumn(name = "feed_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // 저장 전 시간을 자동으로 입력해 주는 메서드
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
