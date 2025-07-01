package com.example.whiplash.user;

import com.example.whiplash.domain.entity.BaseEntity;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.domain.entity.profile.InvestorProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    @Column(name = "user_id")
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    private Integer age;

    private String email;

    private String password;

    @Enumerated(STRING)
    private Role role;

    @Enumerated(STRING)
    private SummaryLevel summaryLevel;

    @Enumerated(STRING)
    private UserStatus userStatus;

    @Enumerated(STRING)
    private SocialProvider socialProvider;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_profile_id")
    private InvestorProfile investorProfile;

//    public void update(UserModifyRequestDTO dto) {
//        this.name = dto.getName();
//        this.age = dto.getAge();
//        this.email = dto.getEmail();
//        this.summaryLevel = dto.getSummaryLevel();
//    }

    public void encodePassword(String password) {
        this.password = password;
    }

    public void setInvestorProfile(InvestorProfile investorProfile) {
        this.investorProfile = investorProfile;
    }

    public void activateUser() {
        this.userStatus = UserStatus.ACTIVE;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}

