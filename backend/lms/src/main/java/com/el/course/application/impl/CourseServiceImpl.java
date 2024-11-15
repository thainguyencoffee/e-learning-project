package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.dto.*;
import com.el.course.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseQueryService courseQueryService;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CourseQueryService courseQueryService,
                             RolesBaseUtil rolesBaseUtil) {
        this.courseRepository = courseRepository;
        this.courseQueryService = courseQueryService;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Course createCourse(String teacher, CourseDTO courseDTO) {
        Course course = courseDTO.toCourse(teacher);
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateInfo(
                courseUpdateDTO.title(),
                courseUpdateDTO.description(),
                courseUpdateDTO.thumbnailUrl(),
                courseUpdateDTO.benefits(),
                courseUpdateDTO.prerequisites(),
                courseUpdateDTO.subtitles());
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.delete();
        courseRepository.save(course);
    }

    @Override
    public void deleteCourseForce(Long courseId) {
        Course course = courseQueryService.findCourseInTrashById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.deleteForce();
        courseRepository.delete(course);
    }

    @Override
    public void restoreCourse(Long courseId) {
        Course course = courseQueryService.findCourseDeleted(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.restore();
        courseRepository.save(course);
    }


    @Override
    public Course assignTeacher(Long courseId, String teacher) {
        Course existsCourse = courseQueryService.findCourseById(courseId);
        existsCourse.assignTeacher(teacher);
        courseRepository.save(existsCourse);
        return existsCourse;
    }


    @Override
    public Long addSection(Long courseId, CourseSectionDTO courseSectionDTO) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        CourseSection courseSection = courseSectionDTO.toCourseSection();
        course.addSection(courseSection);
        courseRepository.save(course);
        return courseSection.getId();
    }

    @Override
    public void updateSectionInfo(Long courseId, Long sectionId, String newTitle) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateSection(sectionId, newTitle);
        courseRepository.save(course);
    }

    @Override
    public void removeSection(Long courseId, Long sectionId) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeSection(sectionId);
        courseRepository.save(course);
    }

    @Override
    public Long addLesson(Long courseId, Long sectionId, Lesson lesson) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.addLessonToSection(sectionId, lesson);
        courseRepository.save(course);
        return lesson.getId();
    }

    @Override
    public void updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateLessonInSection(sectionId, lessonId, updatedLesson);
        courseRepository.save(course);
    }

    @Override
    public void removeLesson(Long courseId, Long sectionId, Long lessonId) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeLessonFromSection(sectionId, lessonId);
        courseRepository.save(course);
    }

    @Override
    public Course updatePrice(Long courseId, MonetaryAmount newPrice) {
        Course course = courseQueryService.findCourseById(courseId);
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }

    private boolean canUpdateCourse(Course course) {
        if (rolesBaseUtil.isAdmin()) {
            return true;
        }

        String currentUserId = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        return course.getTeacher().equals(currentUserId);
    }

    @Override
    public void requestPublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.requestPublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void requestUnpublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.requestUnpublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void approvePublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.approvePublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectPublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.rejectPublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void approveUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.approveUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.rejectUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public Long addPost(Long courseId, CoursePostDTO coursePostDTO) {
        com.el.common.auth.web.dto.UserInfo userInfo = rolesBaseUtil.getCurrentUserInfoFromJwt();

        Post post = coursePostDTO.toPost(new UserInfo(userInfo.firstName(), userInfo.lastName(), userInfo.username()));
        Course course = courseQueryService.findCourseById(courseId);
        course.addPost(post);
        courseRepository.save(course);
        return post.getId();
    }

    @Override
    public void updatePost(Long courseId, Long postId, CoursePostDTO coursePostDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.updatePost(postId, coursePostDTO.content(), coursePostDTO.attachmentUrls());
        courseRepository.save(course);
    }

    @Override
    public void deletePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId);
        course.deletePost(postId);
        courseRepository.save(course);
    }

    @Override
    public void restorePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId);
        course.restorePost(postId);
        courseRepository.save(course);
    }

    @Override
    public void deleteForcePost(Long courseId, Long postId) {
        Course course = courseQueryService.findCourseById(courseId);
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

}
