package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

@Getter
@Table("course_section")
@ToString
public class CourseSection {
    @Id
    private Long id;
    private String title;
    @MappedCollection(idColumn = "course_section")
    private Set<Lesson> lessons = new HashSet<>();
    @MappedCollection(idColumn = "course_section")
    private Set<Quiz> quizzes = new HashSet<>();
    private Integer orderIndex;
    private Boolean published;

    public CourseSection(String title) {
        Assert.hasText(title, "Title must not be empty.");

        this.title = title;
        this.published = true;
    }

    public void updateInfo(String newTitle) {
        if (title.isBlank()) {
            throw new InputInvalidException("Title must not be empty.");
        }
        this.title = newTitle;
    }

    public void addLesson(Lesson lesson) {
        if (this.lessons.stream().anyMatch(l ->
                l.getTitle().equals(lesson.getTitle()) ||
                        (l.getLink() != null && lesson.getLink() != null && l.getLink().equals(lesson.getLink())))) {
            throw new InputInvalidException("Duplicate lesson title or link.");
        }

        int orderIndexLast = 1;
        if (!this.lessons.isEmpty()) {
            orderIndexLast = this.lessons.stream().mapToInt(Lesson::getOrderIndex).max().getAsInt() + 1;
        }
        lesson.setOrderIndex(orderIndexLast);
        this.lessons.add(lesson);
    }

    public void updateLesson(Long lessonId, Lesson updatedLesson) {
        Lesson lesson = findLessonById(lessonId);

        if (this.lessons.stream()
                .filter(l -> !l.getId().equals(lessonId))
                .anyMatch(l -> l.getTitle().equals(updatedLesson.getTitle()) || l.getLink().equals(updatedLesson.getLink()))) {
            throw new InputInvalidException("Duplicate lesson title or link.");
        }

        lesson.updateFrom(updatedLesson);
    }

    public void removeLesson(Long lessonId) {
        Lesson lesson = findLessonById(lessonId);
        this.lessons.remove(lesson);
    }

    public Lesson findLessonById(Long lessonId) {
        return this.lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean hasLessons() {
        return !this.lessons.isEmpty();
    }

    protected void setOrderIndex(int i) {
        this.orderIndex = i;
    }

    public void addQuiz(Quiz quiz) {
        if (this.quizzes.stream().anyMatch(q -> q.getTitle().equals(quiz.getTitle())))
            throw new InputInvalidException("Duplicate quiz title.");

        if (this.quizzes.stream().anyMatch(q -> /*!q.isDeleted() &&*/ q.getAfterLessonId().equals(quiz.getAfterLessonId()))) {
            throw new InputInvalidException("There is already a quiz after the lesson.");
        }

        this.quizzes.add(quiz);
    }

    public void updateQuiz(Long quizId, String newTitle, String newDescription, Integer newPassScorePercent) {
        Quiz quiz = findQuizById(quizId);

        if (this.quizzes.stream()
                .filter(q -> !q.getId().equals(quizId))
                .anyMatch(q -> q.getTitle().equals(newTitle))) {
            throw new InputInvalidException("Duplicate quiz title.");
        }

        quiz.updateInfo(newTitle, newDescription, newPassScorePercent);
    }

    public void addQuestionToQuiz(Long quizId, Question question) {
        Quiz quiz = findQuizById(quizId);
        quiz.addQuestion(question);
    }

    public void updateQuestionInQuiz(Long quizId, Long questionId, Question updatedQuestion) {
        Quiz quiz = findQuizById(quizId);
        quiz.updateQuestion(questionId, updatedQuestion);
    }

    public void deleteQuestionFromQuiz(Long quizId, Long questionId) {
        Quiz quiz = findQuizById(quizId);
        quiz.deleteQuestion(questionId);
    }

    private Quiz findQuizById(Long quizId) {
        return this.quizzes.stream()
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public void deleteQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        quiz.delete();
    }

    public void restoreQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        quiz.restore();
    }

    public void forceDeleteQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        this.quizzes.remove(quiz);
    }


    // Special case called by Course when Course unpublished mode
    public void markAsSectionUnpublished() {
        this.published = false;
    }

    public void markAsSectionPublished() {
        this.published = true;
    }

}