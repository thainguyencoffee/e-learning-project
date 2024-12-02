package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.CourseInTrashDTO;
import com.el.course.application.dto.PostInTrashDTO;
import com.el.course.application.dto.PublishedCourseDTO;
import com.el.course.application.dto.QuizInTrashDTO;
import com.el.course.domain.*;
import com.el.enrollment.domain.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseQueryServiceImpl(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, RolesBaseUtil rolesBaseUtil) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllByDeleted(false, pageable);
        }

        if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findAllByTeacherAndDeleted(teacher, false, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findCourseById(Long courseId, Boolean deleted) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findByIdAndDeleted(courseId, deleted)
                    .orElseThrow(ResourceNotFoundException::new);
        }

        if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findByIdAndDeletedAndTeacher(courseId, deleted, teacher)
                    .orElseThrow(ResourceNotFoundException::new);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<CourseInTrashDTO> findAllCoursesInTrash(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllCoursesInTrash(page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findAllCoursesInTrashByTeacher(teacher, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findPublishedCourseById(Long courseId) {
        return courseRepository.findByIdAndDeletedAndPublished(courseId, false, true)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Page<Course> findAllPublishedCourses(Pageable pageable) {
        return courseRepository.findAllByDeletedAndPublished(false,true, pageable);
    }

    @Override
    public List<PublishedCourseDTO> findAllPublishedCoursesDTO(Pageable pageable) {
        Page<Course> courses = findAllPublishedCourses(pageable);
        return courses.getContent().stream().map(PublishedCourseDTO::fromCourse).toList();
    }

    @Override
    public PublishedCourseDTO findCoursePublishedById(Long courseId) {
        Course course = findPublishedCourseById(courseId);
        return PublishedCourseDTO.fromCourse(course);
    }

    @Override
    public List<Post> findAllPostsByCourseId(Long courseId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin())
            return courseRepository.findAllPostsByCourseId(courseId, page, size);

        if(rolesBaseUtil.isTeacher())
            return courseRepository.findAllPostsByCourseIdAndTeacher(courseId, currentUser, page, size);

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser))
            return courseRepository.findAllPostsByCourseId(courseId, page, size);

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Post findPostByCourseIdAndPostId(Long courseId, Long postId) {
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin())
            return findPost(courseId, postId);

        if(rolesBaseUtil.isTeacher())
            return findPostForTeacher(courseId, postId, currentUser);

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser))
            return findPost(courseId, postId);

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<PostInTrashDTO> findAllPostsInTrash(Long courseId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin())
            return courseRepository.findAllPostsInTrashByCourseId(courseId, page, size);

        if(rolesBaseUtil.isTeacher())
            return courseRepository.findAllPostsInTrashByCourseIdAndTeacher(courseId, currentUser, page, size);

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<Comment> findCommentsByPostId(Long courseId, Long postId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin())
            return courseRepository.findAllCommentsByCourseIdAndPostId(courseId, postId, page, size);

        if(rolesBaseUtil.isTeacher())
            return courseRepository.findAllCommentsByCourseIdAndPostIdAndTeacher(courseId, postId, currentUser, page, size);

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser))
            return courseRepository.findAllCommentsByCourseIdAndPostId(courseId, postId, page, size);

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<Quiz> findAllQuizzes(Long courseId, Long sectionId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllQuizzesByCourseIdAndSectionId(courseId, sectionId, page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findAllQuizzesByCourseIdAndSectionIdAndTeacher(courseId, sectionId, currentUser, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Quiz findQuizById(Long courseId, Long sectionId, Long quizId) {
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findQuizByCourseIdAndSectionIdAndQuizId(courseId, sectionId, quizId)
                    .orElseThrow(ResourceNotFoundException::new);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findQuizByCourseIdAndSectionIdAndQuizIdAndTeacher(courseId, sectionId, quizId, currentUser)
                    .orElseThrow(ResourceNotFoundException::new);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<QuizInTrashDTO> findAllQuizzesInTrash(Long courseId, Long sectionId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllPostsInTrashByCourseIdAndSectionId(courseId, sectionId, page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findAllPostsInTrashByCourseIdAndSectionIdAndTeacher(courseId, sectionId, currentUser, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Quiz findQuizByQuizId(Long quizId) {
        return courseRepository.findQuizByQuizId(quizId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Lesson findLessonByCourseIdAndLessonId(Long courseId, Long lessonId) {
        Course course = findPublishedCourseById(courseId);
        return course.getLessonInSectionForPublishedById(lessonId);
    }

    @Override
    public List<PublishedCourseDTO> searchPublishedCoursesDTO(String query, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<Course> courses = courseRepository.searchPublishedCourses(query, page, size);
        return courses.stream().map(PublishedCourseDTO::fromCourse).toList();
    }

    private boolean isUserEnrolled(Long courseId, String username) {
        return enrollmentRepository.findByCourseIdAndStudent(courseId, username).isPresent();
    }

    private Post findPost(Long courseId, Long postId) {
        return courseRepository.findPostByCourseIdAndPostId(courseId, postId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Post findPostForTeacher(Long courseId, Long postId, String teacher) {
        return courseRepository.findPostByCourseIdAndPostIdAndTeacher(courseId, postId, teacher)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
