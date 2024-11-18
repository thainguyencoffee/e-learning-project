package com.el.enrollment.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Table("quiz_submission")
@Getter
public class QuizSubmission {
    @Id
    private Long id;
    private Long quizId;
//    private Long afterLessonId;
    @MappedCollection(idColumn = "quiz_submission")
    private Set<QuizAnswer> answers = new HashSet<>();
    private Integer score;
    private Instant submittedDate;
    private Instant lastModifiedDate;
    private boolean passed;

    public QuizSubmission(Long quizId, Set<QuizAnswer> answers, Integer score, Boolean passed) {
        if (quizId == null) throw new InputInvalidException("QuizId must not be null.");
        if (answers == null || answers.isEmpty()) throw new InputInvalidException("Answers must not be null or empty.");
        if (score == null || score < 0) throw new InputInvalidException("Score must be a non-negative integer.");
        if (passed == null) throw new InputInvalidException("Passed must not be null.");

        this.quizId = quizId;
        this.score = score;
        this.passed = passed;
        answers.forEach(this::addAnswer);
        this.submittedDate = Instant.now();
        this.lastModifiedDate = Instant.now();
    }

    public void reSubmit(Set<QuizAnswer> answers, Integer score, Boolean passed) {
        if (answers == null || answers.isEmpty()) throw new InputInvalidException("Answers must not be null or empty.");
        if (score == null || score < 0) throw new InputInvalidException("Score must be a non-negative integer.");
        if (passed == null) throw new InputInvalidException("Passed must not be null.");

        this.answers.clear();
        answers.forEach(this::addAnswer);
        this.score = score;
        this.passed = passed;
        lastModifiedDate = Instant.now();
    }

    public void addAnswer(QuizAnswer answer) {
        if (answers.stream().anyMatch(a -> a.getQuestionId().equals(answer.getQuestionId()))) {
            throw new InputInvalidException("Answer for this option already exists.");
        }
        answers.add(answer);
    }

}
