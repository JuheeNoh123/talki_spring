package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import springkong.talki_spring.enums.AlternativeThoughtType;
import springkong.talki_spring.enums.ExpectationVsRealityType;
import springkong.talki_spring.enums.PracticeMode;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private PracticeMode mode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String selectedThoughts; // JSON array of AutomaticThoughtType

    private String customThought;

    private Boolean breathingCompleted;

    @Enumerated(EnumType.STRING)
    private ExpectationVsRealityType expectationVsReality;

    @Enumerated(EnumType.STRING)
    private AlternativeThoughtType alternativeThought;

    private String alternativeThoughtCustom;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String displayThought() {
        if (alternativeThought == AlternativeThoughtType.CUSTOM || alternativeThought == null) {
            return alternativeThoughtCustom;
        }
        return alternativeThought.name();
    }
}
