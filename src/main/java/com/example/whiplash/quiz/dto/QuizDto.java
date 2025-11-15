package com.example.whiplash.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto implements Serializable {

    private String question;

    private List<String> options;

    @JsonProperty("answer_index")
    private Integer answerIndex;

    private String explanation;
}
