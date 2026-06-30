package com.example.whiplash.quiz.repository;

import com.example.whiplash.MongoTestSupport;
import com.example.whiplash.quiz.document.TermQuizPool;
import com.example.whiplash.quiz.dto.QuizDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TermQuizPoolRepositoryTest extends MongoTestSupport {

    @Autowired
    private TermQuizPoolRepository repository;

    private List<QuizDto> makeQuizzes(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new QuizDto(
                        "질문 " + i,
                        List.of("A", "B", "C", "D"),
                        i % 4,
                        "해설 " + i
                ))
                .toList();
    }

    @Test
    @DisplayName("저장한 퀴즈 풀을 termName으로 조회할 수 있다")
    void findByTermName_returnsPool() {
        // given
        TermQuizPool pool = TermQuizPool.create("ETF", makeQuizzes(12));
        repository.save(pool);

        // when
        Optional<TermQuizPool> found = repository.findByTermName("ETF");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTermName()).isEqualTo("ETF");
        assertThat(found.get().getQuizzes()).hasSize(12);
        assertThat(found.get().getGeneratedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 termName 조회 시 empty를 반환한다")
    void findByTermName_returnsEmpty_whenNotFound() {
        Optional<TermQuizPool> found = repository.findByTermName("존재하지않는용어");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByTermName은 풀이 있으면 true, 없으면 false를 반환한다")
    void existsByTermName_returnsCorrectResult() {
        repository.save(TermQuizPool.create("PER", makeQuizzes(5)));

        assertThat(repository.existsByTermName("PER")).isTrue();
        assertThat(repository.existsByTermName("PBR")).isFalse();
    }

    @Test
    @DisplayName("updateQuizzes 호출 시 퀴즈 목록과 generatedAt이 갱신된다")
    void updateQuizzes_updatesPoolAndTimestamp() throws InterruptedException {
        // given
        TermQuizPool pool = TermQuizPool.create("금리", makeQuizzes(3));
        repository.save(pool);

        TermQuizPool saved = repository.findByTermName("금리").orElseThrow();
        var originalTime = saved.getGeneratedAt();

        Thread.sleep(10); // 시간 차이 확보

        // when
        saved.updateQuizzes(makeQuizzes(12));
        repository.save(saved);

        // then
        TermQuizPool updated = repository.findByTermName("금리").orElseThrow();
        assertThat(updated.getQuizzes()).hasSize(12);
        assertThat(updated.getGeneratedAt()).isAfter(originalTime);
    }

    @Test
    @DisplayName("quizzes 필드의 모든 속성이 MongoDB 저장/조회 후 보존된다")
    void quizDto_fieldsArePreserved_afterRoundTrip() {
        // given
        QuizDto quiz = new QuizDto("ETF란 무엇인가?", List.of("A", "B", "C", "D"), 2, "상장지수펀드입니다.");
        repository.save(TermQuizPool.create("ETF2", List.of(quiz)));

        // when
        QuizDto restored = repository.findByTermName("ETF2").orElseThrow()
                .getQuizzes().get(0);

        // then
        assertThat(restored.getQuestion()).isEqualTo("ETF란 무엇인가?");
        assertThat(restored.getOptions()).containsExactly("A", "B", "C", "D");
        assertThat(restored.getAnswerIndex()).isEqualTo(2);
        assertThat(restored.getExplanation()).isEqualTo("상장지수펀드입니다.");
    }
}