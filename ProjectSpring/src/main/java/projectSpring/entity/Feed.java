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
    private int height; // 핀터레스트 레이아웃용 고유 높이 값 (예: 250, 350)

    @Column(name = "is_collab", nullable = false)
    private boolean isCollab; // 🤝 협업 모집 여부 (true/false)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl; // 💡 이미지 경로 저장할 컬럼

    // 💡 고급 기능: PostgreSQL의 장점을 살린 JSON 확장 컬럼 (선택 사항)
    // 음악의 BPM이나 아트의 해상도 등 타입별 가변 데이터를 스키마 변경 없이 JSON으로 저장할 수 있습니다.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    // 태그와의 다대다(Many-to-Many) 관계 설정
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
