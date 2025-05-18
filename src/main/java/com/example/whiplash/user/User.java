package com.example.whiplash.user;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
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
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    private Integer age;

    private String email;

    @Enumerated(STRING)
    private Role role;

    @Enumerated(STRING)
    private SummaryLevel summaryLevel;

    public void update(UserModifyRequestDTO dto) {
        this.name = dto.getName();
        this.age = dto.getAge();
        this.email = dto.getEmail();
        this.summaryLevel = dto.getSummaryLevel();
    }
}

