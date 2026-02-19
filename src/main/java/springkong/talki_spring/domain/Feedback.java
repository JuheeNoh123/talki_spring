package springkong.talki_spring.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presentation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Presentation presentation;

    private Double speechScore;

    private Double postureScore;

    private Double gazeScore;

    @Column(columnDefinition = "TEXT")
    private String detailJson;

    private LocalDateTime createdAt;
}
