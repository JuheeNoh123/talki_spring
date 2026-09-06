package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import springkong.talki_spring.enums.PracticeType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSubStepResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PracticeSession practiceSession;

    @Enumerated(EnumType.STRING)
    private PracticeType subStepType;

    private Integer score;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String feedbackText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawResultJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
