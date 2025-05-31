package com.example.whiplash.domain.entity.profile;


import com.example.whiplash.article.document.Category;
import com.example.whiplash.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection //TODO: 값타입 매핑 확인 필요
    @Enumerated(STRING)
    private List<Category> interestCategories = new ArrayList<>();

    // 단방향: Profile → User
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
