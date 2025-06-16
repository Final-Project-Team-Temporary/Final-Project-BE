package com.example.whiplash.domain.entity;

import jakarta.persistence.*;
import lombok.*;

//TODO: Keyword entity는 수정 불가.
//이유: Keyword d의 name을 UserKeyword에게 넘겨줄거임. but, Keyword.name이 변경되면 반영이 안됨

@Entity
@Table(name = "keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Keyword {
    @Column(name = "keyword_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}

