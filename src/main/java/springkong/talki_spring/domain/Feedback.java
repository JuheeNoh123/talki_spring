package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id")
    private Presentation presentation;

    // ===== 핵심 점수 =====
    private Integer totalScore;

    private Double gazeScore;
    private Double speechScore;
    private Double postureScore;

    // ===== 핵심 지표 =====
    private Double speechWpm;
    private Double poseWarningRatio;
    private Double gazeFrontRatio;

    // ===== JSON 저장 =====
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawJson;   // raw 전체

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String llmFeedbackJson; // LLM 피드백 전체
}