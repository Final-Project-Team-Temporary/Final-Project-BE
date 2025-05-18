package com.example.whiplash.domain.profile;


import com.example.whiplash.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "investor_profiles")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
public class InvestorProfile {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    private InvestorType investorType;

    @Enumerated(STRING)
    private RiskTolerance riskTolerance;

    private Integer investmentPeriod;
    private Integer expectedReturn;

    @Enumerated(value = STRING)
    private ExperienceLevel experienceLevel;

    // 단방향: Profile → User
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
