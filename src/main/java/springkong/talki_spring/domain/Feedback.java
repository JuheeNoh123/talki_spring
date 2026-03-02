package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id", unique = true)
    private Presentation presentation;

    // nullable 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    private LocalDateTime createdAt;

    // ===== 점수 =====
    private Integer totalScore;
    private Double gazeScore;
    private Double speechScore;
    private Double postureScore;
    private Double fillerScore;

    // ===== 핵심 KPI =====
    private Double speechWpm;
    private Double gazeFrontRatio;
    private Double poseWarningRatio;

    // ===== LLM =====
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String llmFeedbackJson;

    // ===== RAW 전체 데이터 =====
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawDataJson;
}