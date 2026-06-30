package com.example.whiplash.quiz.document;

import com.example.whiplash.quiz.dto.QuizDto;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB: term_quiz_pools 컬렉션
 * 용어 단위로 퀴즈 풀을 저장 (userId 무관, 전체 공유)
 */
@Document(collection = "term_quiz_pools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TermQuizPool {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("term_name")
    private String termName;

    private List<QuizDto> quizzes;

    @Field("pool_size")
    private int poolSize;

    @Field("generated_at")
    private LocalDateTime generatedAt;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    public static TermQuizPool create(String termName, List<QuizDto> quizzes) {
        return TermQuizPool.builder()
                .termName(termName)
                .quizzes(quizzes)
                .poolSize(quizzes.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public void updateQuizzes(List<QuizDto> newQuizzes) {
        this.quizzes = newQuizzes;
        this.poolSize = newQuizzes.size();
        this.generatedAt = LocalDateTime.now();
    }
}