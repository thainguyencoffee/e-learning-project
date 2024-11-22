package com.el.course.application;

import com.el.course.application.dto.*;
import com.el.course.domain.Course;
import com.el.course.domain.Lesson;
import com.el.course.domain.QuizCalculationResult;

import javax.money.MonetaryAmount;
import java.util.Map;

public interface CourseService {

    Course createCourse(String teacher, CourseDTO courseDTO);

    Course updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO);

    void deleteCourse(Long courseId);

    void deleteCourseForce(Long courseId);

    void restoreCourse(Long courseId);

    Course assignTeacher(Long courseId, String teacher);

    Long addSection(Long courseId, CourseSectionDTO courseSectionDTO);

    void updateSectionInfo(Long courseId, Long sectionId, String newTitle);

    void removeSection(Long courseId, Long sectionId);

    Long addLesson(Long courseId, Long sectionId, Lesson lesson);

    void updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson);

    void removeLesson(Long courseId, Long sectionId, Long lessonId);

    Course updatePrice(Long courseId, MonetaryAmount newPrice);

    void requestPublish(Long courseId, CourseRequestDTO courseRequestDTO);

    void requestUnpublish(Long courseId, CourseRequestDTO courseRequestDTO);

    void approvePublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO);

    void rejectPublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO);

    void approveUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO);

    void rejectUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO);

    Long addPost(Long courseId, CoursePostDTO coursePostDTO);

    void updatePost(Long courseId, Long postId, CoursePostDTO coursePostDTO);

    void deletePost(Long courseId, Long postId);

    void restorePost(Long courseId, Long postId);

    void deleteForcePost(Long courseId, Long postId);

    Long addComment(Long courseId, Long postId, CommentDTO commentDTO);

    void updateComment(Long courseId, Long postId, Long commentId, CommentDTO commentDTO);

    void deleteComment(Long courseId, Long postId, Long commentId);

    Long addEmotion(Long courseId, Long postId);

    Long addQuizToSection(Long courseId, Long sectionId, QuizDTO quizDTO);

    void updateQuiz(Long courseId, Long sectionId, Long quizId, QuizUpdateDTO quizUpdateDTO);

    void deleteQuiz(Long courseId, Long sectionId, Long quizId);

    void restoreQuiz(Long courseId, Long sectionId, Long quizId);

    void deleteForceQuiz(Long courseId, Long sectionId, Long quizId);

    Long addQuestionToQuiz(Long courseId, Long sectionId, Long quizId, QuestionDTO questionDTO);

    void updateQuestion(Long courseId, Long sectionId, Long quizId, Long questionId, QuestionDTO questionUpdateDTO);

    void deleteQuestion(Long courseId, Long sectionId, Long quizId, Long questionId);

    /**
     * This method will return {@code QuizCalculationResult} with detail. <br>
     * score: The score of {@code answers } <br>
     * passed: Is {@code  answers } pass or not */
    QuizCalculationResult calculateQuizScore(Long courseId, Long quizId, Map<Long, Object> userAnswers);

    Long addReview(Long courseId, Long enrollmentId, ReviewDTO reviewDTO);

    void deleteReview(Long courseId, String student);

}
