package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.EnsureEnrollmentCompleted;
import com.el.course.domain.*;
import com.el.course.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.util.Map;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseQueryService courseQueryService;
    private final RolesBaseUtil rolesBaseUtil;
    private final EnsureEnrollmentCompleted ensureEnrollmentCompleted;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CourseQueryService courseQueryService,
                             RolesBaseUtil rolesBaseUtil,
                             EnsureEnrollmentCompleted ensureEnrollmentCompleted) {
        this.courseRepository = courseRepository;
        this.courseQueryService = courseQueryService;
        this.rolesBaseUtil = rolesBaseUtil;
        this.ensureEnrollmentCompleted = ensureEnrollmentCompleted;
    }

    @Override
    public Long createCourse(String teacher, CourseDTO courseDTO) {
        Course course = courseDTO.toCourse(teacher);
        courseRepository.save(course);
        return course.getId();
    }

    @Override
    public void updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateInfo(
                courseUpdateDTO.title(),
                courseUpdateDTO.description(),
                courseUpdateDTO.thumbnailUrl(),
                courseUpdateDTO.benefits(),
                courseUpdateDTO.prerequisites(),
                courseUpdateDTO.subtitles());
        courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.delete();
        courseRepository.save(course);
    }

    @Override
    public void deleteCourseForce(Long courseId) {
        Course course = courseQueryService.findCourseById(courseId, true);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.deleteForce();
        courseRepository.delete(course);
    }

    @Override
    public void restoreCourse(Long courseId) {
        Course course = courseQueryService.findCourseById(courseId, true);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.restore();
        courseRepository.save(course);
    }


    @Override
    public void assignTeacher(Long courseId, String teacher) {
        if (!rolesBaseUtil.isAdmin()) {
            throw new AccessDeniedException("Only admin can assign teacher to course");
        }
        Course existsCourse = courseQueryService.findCourseById(courseId, false);
        existsCourse.assignTeacher(teacher);
        courseRepository.save(existsCourse);
    }


    @Override
    public Long addSection(Long courseId, CourseSectionDTO courseSectionDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        CourseSection courseSection = courseSectionDTO.toCourseSection();
        course.addSection(courseSection);
        courseRepository.save(course);
        return courseSection.getId();
    }

    @Override
    public void updateSectionInfo(Long courseId, Long sectionId, String newTitle) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateSection(sectionId, newTitle);
        courseRepository.save(course);
    }

    @Override
    public void removeSection(Long courseId, Long sectionId) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeSection(sectionId);
        courseRepository.save(course);
    }

    @Override
    public Long addLesson(Long courseId, Long sectionId, Lesson lesson) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.addLessonToSection(sectionId, lesson);
        courseRepository.save(course);
        return lesson.getId();
    }

    @Override
    public void updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateLessonInSection(sectionId, lessonId, updatedLesson);
        courseRepository.save(course);
    }

    @Override
    public void removeLesson(Long courseId, Long sectionId, Long lessonId) {
        Course course = courseQueryService.findCourseById(courseId, false);

        if (cannotUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeLessonFromSection(sectionId, lessonId);
        courseRepository.save(course);
    }

    @Override
    public Course updatePrice(Long courseId, MonetaryAmount newPrice) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }

    @Override
    public void requestPublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.requestPublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void requestUnpublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.requestUnpublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void approvePublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.approvePublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectPublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.rejectPublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void approveUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.approveUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.rejectUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public Long addPost(Long courseId, CoursePostDTO coursePostDTO) {
        com.el.common.auth.web.dto.UserInfo userInfo = rolesBaseUtil.getCurrentUserInfoFromJwt();

        Post post = coursePostDTO.toPost(new UserInfo(userInfo.firstName(), userInfo.lastName(), userInfo.username()));
        Course course = courseQueryService.findCourseById(courseId, false);
        course.addPost(post);
        courseRepository.save(course);
        return post.getId();
    }

    @Override
    public void updatePost(Long courseId, Long postId, CoursePostDTO coursePostDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.updatePost(postId, coursePostDTO.content(), coursePostDTO.attachmentUrls());
        courseRepository.save(course);
    }

    @Override
    public void deletePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.deletePost(postId);
        courseRepository.save(course);
    }

    @Override
    public void restorePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.restorePost(postId);
        courseRepository.save(course);
    }

    @Override
    public void deleteForcePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.forceDeletePost(postId);
        courseRepository.save(course);
    }

    @Override
    public Long addComment(Long courseId, Long postId, CommentDTO commentDTO) {
        com.el.common.auth.web.dto.UserInfo userInfo = rolesBaseUtil.getCurrentUserInfoFromJwt();

        Comment comment = commentDTO.toComment(new UserInfo(userInfo.firstName(), userInfo.lastName(), userInfo.username()));
        Course course = courseQueryService.findPublishedCourseById(courseId);
        course.addCommentToPost(postId, comment);
        courseRepository.save(course);
        return comment.getId();
    }

    @Override
    public void updateComment(Long courseId, Long postId, Long commentId, CommentDTO commentDTO) {
        Course course = courseQueryService.findPublishedCourseById(courseId);
        course.updateComment(postId, commentId, commentDTO.content(), commentDTO.attachmentUrls());
        courseRepository.save(course);
    }

    @Override
    public void deleteComment(Long courseId, Long postId, Long commentId) {
        String username = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        Course course = courseQueryService.findPublishedCourseById(courseId);
        course.deleteCommentFromPost(postId, commentId, username);
        courseRepository.save(course);
    }

    @Override
    public Long addEmotion(Long courseId, Long postId) {
        String username = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        Emotion emotion = new Emotion(username);
        Course course = courseQueryService.findPublishedCourseById(courseId);
        course.addEmotionToPost(postId, emotion);
        courseRepository.save(course);
        return emotion.getId();
    }

    @Override
    public Long addQuizToSection(Long courseId, Long sectionId, QuizDTO quizDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        Quiz quiz = quizDTO.toQuiz();
        course.addQuizToSection(sectionId, quiz);
        courseRepository.save(course);
        return quiz.getId();
    }

    @Override
    public void updateQuiz(Long courseId, Long sectionId, Long quizId, QuizUpdateDTO quizUpdateDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.updateQuizInSection(sectionId, quizId,
                quizUpdateDTO.title(),
                quizUpdateDTO.description(),
                quizUpdateDTO.passScorePercentage());
        courseRepository.save(course);
    }

    @Override
    public void deleteQuiz(Long courseId, Long sectionId, Long quizId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.deleteQuizFromSection(sectionId, quizId);
        courseRepository.save(course);
    }

    @Override
    public void restoreQuiz(Long courseId, Long sectionId, Long quizId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.restoreQuizInSection(sectionId, quizId);
        courseRepository.save(course);
    }

    @Override
    public void deleteForceQuiz(Long courseId, Long sectionId, Long quizId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.forceDeleteQuizFromSection(sectionId, quizId);
        courseRepository.save(course);
    }

    @Override
    public Long addQuestionToQuiz(Long courseId, Long sectionId, Long quizId, QuestionDTO questionDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        Question question = questionDTO.toQuestion();
        course.addQuestionToQuizInSection(sectionId, quizId, question);
        courseRepository.save(course);
        return question.getId();
    }

    @Override
    public void updateQuestion(Long courseId, Long sectionId, Long quizId, Long questionId, QuestionDTO questionUpdateDTO) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.updateQuestionInQuizInSection(sectionId, quizId, questionId, questionUpdateDTO.toQuestion());
        courseRepository.save(course);
    }

    @Override
    public void deleteQuestion(Long courseId, Long sectionId, Long quizId, Long questionId) {
        Course course = courseQueryService.findCourseById(courseId, false);
        course.deleteQuestionFromQuizInSection(sectionId, quizId, questionId);
        courseRepository.save(course);
    }

    /**
     * @param userAnswers with three overloads for different types of questions
     * */
    @Override
    public QuizCalculationResult calculateQuizScore(Long courseId, Long quizId, Map<Long, Object> userAnswers) {
        Course course = courseQueryService.findPublishedCourseById(courseId);
        return course.calculateQuiz(quizId, userAnswers);
    }

    @Override
    public Long addReview(Long courseId, Long enrollmentId, ReviewDTO reviewDTO) {
        Course course = courseQueryService.findPublishedCourseById(courseId);

        if (rolesBaseUtil.isTeacher() || rolesBaseUtil.isAdmin()) {
            throw new AccessDeniedException("Teacher and Admin cannot add review to course");
        }
        String username = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        ensureEnrollmentCompleted.ensureEnrollmentCompleted(enrollmentId, username);

        Review review = reviewDTO.toReview(username);
        course.addReview(review);
        courseRepository.save(course);
        return review.getId();
    }

    @Override
    public void deleteReview(Long courseId, String student) {
        Course course = courseQueryService.findById(courseId);

        course.deleteReview(student);
        courseRepository.save(course);

    }

    private boolean cannotUpdateCourse(Course course) {
        if (rolesBaseUtil.isAdmin()) {
            return false;
        }

        String currentUserId = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        return !course.getTeacher().equals(currentUserId);
    }

}
