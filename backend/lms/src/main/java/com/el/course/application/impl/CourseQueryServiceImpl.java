package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.domain.*;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseQueryServiceImpl(CourseRepository courseRepository, CourseEnrollmentRepository courseEnrollmentRepository, RolesBaseUtil rolesBaseUtil) {
        this.courseRepository = courseRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllByDeleted(false, pageable);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findAllByTeacherAndDeleted(teacher, false, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Page<Course> findTrashedCourses(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllByDeleted(true, pageable);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findAllByTeacherAndDeleted(teacher, true, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findCourseById(Long courseId) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findByIdAndDeleted(courseId, false)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findByTeacherAndIdAndDeleted(teacher, courseId, false)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findCourseInTrashById(Long courseId) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findByIdAndDeleted(courseId, true)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return courseRepository.findByTeacherAndIdAndDeleted(teacher, courseId, true)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findCourseDeleted(Long courseId) {
        return courseRepository.findByIdAndDeleted(courseId, true)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Course findPublishedCourseById(Long courseId) {
        return courseRepository.findByIdAndPublishedAndDeleted(courseId, true, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<CourseWithoutSectionsDTO> findAllCourseWithoutSectionsDTOs(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        return courseRepository.findAllByPublishedAndDeleted(true, false, page, size);
    }

    @Override
    public CourseWithoutSectionsDTO findCourseWithoutSectionsDTOById(Long courseId) {
        return courseRepository.findCourseWithoutSectionsDTOByIdAndPublishedAndDeleted(courseId, true, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<Post> findAllPostsByCourseId(Long courseId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllPostsByCourseIdAndDeleted(courseId, false, page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findAllPostsByCourseIdAndTeacherAndDeleted(courseId, currentUser, false, page, size);
        }

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser)) {
            return courseRepository.findAllPostsByCourseIdAndDeleted(courseId, false, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Post findPostByCourseIdAndPostId(Long courseId, Long postId) {
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return findPost(courseId, postId);
        }

        if(rolesBaseUtil.isTeacher()) {
            return findPostForTeacher(courseId, postId, currentUser);
        }

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser)) {
            return findPost(courseId, postId);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<Post> findTrashedPosts(Long courseId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllPostsByCourseIdAndDeleted(courseId, true, page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findAllPostsByCourseIdAndTeacherAndDeleted(courseId, currentUser, true, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<Comment> findCommentsByPostId(Long courseId, Long postId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllCommentsByCourseIdAndPostId(courseId, postId, page, size);
        }

        if(rolesBaseUtil.isTeacher()) {
            return courseRepository.findAllCommentsByCourseIdAndPostIdAndTeacher(courseId, postId, currentUser, page, size);
        }

        if (rolesBaseUtil.isUser() && isUserEnrolled(courseId, currentUser)) {
            return courseRepository.findAllCommentsByCourseIdAndPostId(courseId, postId, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    private boolean isUserEnrolled(Long courseId, String username) {
        return courseEnrollmentRepository.findByCourseIdAndStudent(courseId, username).isPresent();
    }

    private Post findPost(Long courseId, Long postId) {
        return courseRepository.findPostByCourseIdAndPostIdAndDeleted(courseId, postId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Post findPostForTeacher(Long courseId, Long postId, String teacher) {
        return courseRepository.findPostByCourseIdAndPostIdAndTeacherAndDeleted(courseId, postId, teacher, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
