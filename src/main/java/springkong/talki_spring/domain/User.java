package springkong.talki_spring.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import springkong.talki_spring.enums.UserType;

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
    private UserType userType;

    public void updateProfile(String userName, String email) {
        if (userName != null) this.userName = userName;
        if (email != null) this.email = email;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // setter 대신 메서드로 수정 권장
    public void updateProfileImage(String key) {
        this.profileImageKey = key;
    }

}
