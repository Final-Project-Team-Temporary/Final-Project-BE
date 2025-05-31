package com.example.whiplash.user;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserModifyRequestDTO {
    private String name;

    private Integer age;

    private String email;

    private SummaryLevel summaryLevel;
}
