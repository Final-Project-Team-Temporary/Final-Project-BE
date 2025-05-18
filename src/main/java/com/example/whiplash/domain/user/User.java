package com.example.whiplash.domain.user;

import com.example.whiplash.domain.history.email.SummaryLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String email;

    @Enumerated(STRING)
    private Role role;

    @Enumerated(STRING)
    private SummaryLevel summaryLevel;
}
