package com.example.whiplash.term.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import com.example.whiplash.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_terms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_terms",
                columnNames = {"user_id", "terms_id"}
        ))
public class UserTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    private String articleId;

}
