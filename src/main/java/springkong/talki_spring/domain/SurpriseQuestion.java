package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "surprise_question")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurpriseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Presentation presentation;

    @Column(nullable = false)
    private String questionId;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String question;

    private Double askedAtSeconds;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String answerText;

    private Boolean answered;

    private Integer contentScore;
    private Integer gptScore;
    private Integer similarityScore;
    private Integer qualityScore;
    private Integer coherenceScore;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String feedback;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
