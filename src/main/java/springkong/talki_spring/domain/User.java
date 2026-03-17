package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import springkong.talki_spring.enums.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    private String password;
    private String email;
    private String userName;
    private String profileImageKey;
    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;
    @Column(name = "streak_days")
    private int streakDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "last_practice_date")
    private LocalDate lastPracticeDate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String userName, String userId, String email) {
        if (userName != null) this.userName = userName;
        if (userId != null) this.userId = userId;
        if (email != null) this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    // setter 대신 메서드로 수정 권장
    public void updateProfileImage(String key) {
        this.profileImageKey = key;
    }

    public void updateUserType(UserType userType) {
        this.userType = userType;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStreakDays(LocalDate today) {
        // 처음 연습하는 경우
        if (this.lastPracticeDate == null) {
            this.streakDays = 1;
        }
        // 이미 오늘 연습한 경우 → 증가 X
        else if (this.lastPracticeDate.equals(today)) {
            return;
        }
        // 어제 연습한 경우 → +1
        else if (this.lastPracticeDate.equals(today.minusDays(1))) {
            this.streakDays += 1;
        }
        // 끊긴 경우 → 1로 초기화
        else {
            this.streakDays = 1;
        }

        this.lastPracticeDate = today;
    }

}
