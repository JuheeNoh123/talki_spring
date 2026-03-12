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
    private Integer totalScore; //총점수
    private Double gazeScore; //시선 점수
    private Double speechScore; //발화 점수
    private Double postureScore; //제스쳐 점수
    private Double fillerScore; //필러 점수
    private Double topicScore; //주제 적합성

    // ===== 핵심 KPI =====
    private Double speechWpm; //발화속도
    private Double gazeFrontRatio; //중앙 시선 비율
    private Double poseWarningRatio; //자세 경고 비율

    // ===== LLM =====
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String llmFeedbackJson;

    // ===== RAW 전체 데이터 =====
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawDataJson;
}