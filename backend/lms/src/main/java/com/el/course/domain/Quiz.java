package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Table("quiz")
@Getter
public class Quiz {
    @Id
    private Long id;
    private String title;
    private String description;
    private Long afterLessonId;
    @MappedCollection(idColumn = "quiz")
    private Set<Question> questions = new HashSet<>();
    private Integer totalScore;
    private Integer passScorePercentage;
    private boolean deleted = false;

    public Quiz(String title, String description, Long afterLessonId, Integer passScorePercentage) {
        if (title == null || title.isBlank())
            throw new InputInvalidException("Title of a quiz must not be empty.");

        if (passScorePercentage == null || passScorePercentage < 0 || passScorePercentage > 100)
            throw new InputInvalidException("Pass score percent must be between 0 and 100.");

        this.title = title;
        this.description = description;
        this.afterLessonId = afterLessonId;
        this.passScorePercentage = passScorePercentage;
        this.totalScore = 0;
    }

    public void updateInfo(String newTitle, String newDescription, Integer newPassScorePercent) {
        if (newTitle == null || newTitle.isBlank())
            throw new InputInvalidException("Title of a quiz must not be empty.");

        if (newPassScorePercent == null || newPassScorePercent < 0 || newPassScorePercent > 100)
            throw new InputInvalidException("Pass score percent must be between 0 and 100.");

        this.title = newTitle;
        this.description = newDescription;
        this.passScorePercentage = newPassScorePercent;
    }

    public void addQuestion(Question question) {
        if (this.questions.stream().anyMatch(q -> q.getContent().equals(question.getContent())))
            throw new InputInvalidException("Duplicate question content.");

        this.questions.add(question);
        this.totalScore += question.getScore();
    }

    public void updateQuestion(Long questionId, Question updatedQuestion) {
        Question question = findQuestionById(questionId);

        if (this.questions.stream()
                .filter(q -> !q.getId().equals(questionId))  // Exclude the current question
                .anyMatch(q -> q.getContent().equals(updatedQuestion.getContent()))) {
            throw new InputInvalidException("Duplicate question content.");
        }

        this.totalScore -= question.getScore();
        this.totalScore += updatedQuestion.getScore();
        question.updateFrom(updatedQuestion);
    }

    public void deleteQuestion(Long questionId) {
        Question question = findQuestionById(questionId);
        this.totalScore -= question.getScore();
        this.questions.remove(question);
    }

    public void delete() {
        if (this.deleted)
            throw new InputInvalidException("This quiz has already been deleted.");

        this.deleted = true;
    }

    public void restore() {
        if (!this.deleted)
            throw new InputInvalidException("This quiz has not been deleted.");

        this.deleted = false;
    }

    // All answers must be provided, no optional
    public int calculateScore(Map<Long, Object> answers) {
        int score = 0;

        for (Question question : questions) {
            Object userAnswer = answers.get(question.getId());
            if (question.getType() == QuestionType.TRUE_FALSE) {
                if (userAnswer instanceof Boolean userTrueFalseAnswer) {
                    if (userTrueFalseAnswer.equals(question.getTrueFalseAnswer()))
                        score += question.getScore();
                } else {
                    throw new InputInvalidException("Quiz calculation error: User answer for true/false must be boolean type.");
                }
            } else if (question.getType() == QuestionType.SINGLE_CHOICE) {
                if (userAnswer instanceof Long singleChoiceAnswerId) {
                    if (question.getOptions().stream()
                            .filter(AnswerOption::getCorrect)
                            .map(AnswerOption::getId)
                            .findFirst()
                            .orElseThrow(ResourceNotFoundException::new)
                            .equals(singleChoiceAnswerId)) {
                        score += question.getScore();
                    }
                } else {
                    throw new InputInvalidException("Quiz calculation error: Single choice question must have exactly one answer.");
                }
            } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                if (userAnswer instanceof Set<?> userAnswerOptions) {
                    Set<Long> correctOptionIds = question.getOptions().stream()
                            .filter(AnswerOption::getCorrect)
                            .map(AnswerOption::getId)
                            .collect(Collectors.toSet());

                    long selectedCorrectAnswers = userAnswerOptions.stream()
                            .filter(correctOptionIds::contains)
                            .count();
                    score += (int) ((double) selectedCorrectAnswers / correctOptionIds.size() * question.getScore());
                } else {
                    throw new InputInvalidException("Quiz calculation error: Multiple choice question must have multiple answers.");
                }
            } else {
                throw new InputInvalidException("Quiz calculation error: Unknown question type.");
            }
        }
        return score;
    }


    private Question findQuestionById(Long questionId) {
        return this.questions.stream()
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Boolean isPassed(Integer score) {
        return (double) score / this.totalScore * 100 >= this.passScorePercentage;
    }
}
