package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 1:1 관계
    @OneToOne
    @JoinColumn(name = "presentation_id")
    private Presentation presentation;

    // 🔥 점수는 따로 컬럼 (조회 빠르게 하기 위함)
    private Integer totalScore;

    // 🔥 raw_result 전체 JSON 저장
    @Column(columnDefinition = "LONGTEXT")
    private String rawJson;

    // 🔥 feedback 전체 JSON 저장
    @Column(columnDefinition = "LONGTEXT")
    private String feedbackJson;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
