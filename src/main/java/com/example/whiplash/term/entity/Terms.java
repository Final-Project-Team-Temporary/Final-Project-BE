package com.example.whiplash.term.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "terms",
        indexes = @Index(name = "idx_term_name", columnList = "termName"),
        uniqueConstraints = @UniqueConstraint(columnNames = "termName"))
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String termName;

    private String AiExplanation;

    private String termCategory;

}
