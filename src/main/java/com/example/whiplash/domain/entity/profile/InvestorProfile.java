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

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "investor_profile_id")
    private Long id;

    @Enumerated(STRING)
    private AgeRange ageRange;

    @Enumerated(STRING)
    private InvestmentLevel investmentLevel;

    @Enumerated(STRING)
    private InvestmentGoal investmentGoal;

    @Enumerated(STRING)
    private RiskTolerance riskTolerance;

    @ElementCollection
    @Enumerated(STRING)
    @CollectionTable(name = "investor_profile_interest_categories",
            joinColumns = @JoinColumn(name = "investor_profile_id"))
    @Column(name = "interest_category")
    private List<Category> interestCategories = new ArrayList<>();

    // 단방향: Profile → User
    @OneToOne(mappedBy = "investorProfile")
    private User user;
}
