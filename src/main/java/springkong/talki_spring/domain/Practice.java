package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import springkong.talki_spring.enums.PracticeType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Practice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private PracticeType practiceType;

    private String thoughtRecognition;
    private String behavioralTestResults;
    private String mindSetting;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Practice(User user, PracticeType practiceType, String thoughtRecognition, String behavioralTestResults, String mindSetting) {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
