package com.el.coursepath.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.coursepath.application.CoursePathQueryService;
import com.el.coursepath.application.dto.CoursePathPublishedDto;
import com.el.coursepath.application.dto.CoursePathPublishedDto.CourseOrderPublishedDto;
import com.el.coursepath.domain.CoursePath;
import com.el.coursepath.domain.CoursePathRepository;
import com.el.coursepath.web.dto.CoursePathInTrashDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CoursePathQueryServiceImpl implements CoursePathQueryService {

    private final CoursePathRepository coursePathRepository;
    private final CourseQueryService courseQueryService;
    private final RolesBaseUtil rolesBaseUtil;

    public CoursePathQueryServiceImpl(CoursePathRepository coursePathRepository,
                                      CourseQueryService courseQueryService,
                                      RolesBaseUtil rolesBaseUtil) {
        this.coursePathRepository = coursePathRepository;
        this.courseQueryService = courseQueryService;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public CoursePath getCoursePath(Long coursePathId) {
        if (rolesBaseUtil.isAdmin()) {
            return coursePathRepository.findByIdAndDeleted(coursePathId, false)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        if (rolesBaseUtil.isTeacher()) {
            String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return coursePathRepository.findByIdAndTeacherAndDeleted(coursePathId, currentUsername, false)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public CoursePath getCoursePath(Long coursePathId, boolean deleted) {
        if (rolesBaseUtil.isAdmin()) {
            return coursePathRepository.findByIdAndDeleted(coursePathId, deleted)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        if (rolesBaseUtil.isTeacher()) {
            String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return coursePathRepository.findByIdAndTeacherAndDeleted(coursePathId, currentUsername, deleted)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<CoursePath> getCoursePaths(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return coursePathRepository.findAllByDeleted(false, pageable);
        }
        if (rolesBaseUtil.isTeacher()) {
            String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return coursePathRepository.findAllByTeacherAndDeleted(currentUsername, false, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public CoursePathPublishedDto getCoursePathPublishedById(Long coursePathId) {
        CoursePath coursePath = coursePathRepository.findByIdAndPublished(coursePathId, true)
                .orElseThrow(ResourceNotFoundException::new);
        Set<CourseOrderPublishedDto> courseOrders = getCourseOrderPublishedDto(coursePath);
        return new CoursePathPublishedDto(
                coursePath.getId(),
                coursePath.getTitle(),
                coursePath.getDescription(),
                courseOrders,
                coursePath.getTeacher(),
                coursePath.getPublishedDate()
        );
    }

    private Set<CourseOrderPublishedDto> getCourseOrderPublishedDto(CoursePath coursePath) {
        return coursePath.getCourseOrders().stream()
                .map(courseOrder -> {
                    var course = courseQueryService.findPublishedCourseById(courseOrder.getCourseId());
                    return Pair.of(course, courseOrder);
                })
                .map(pair -> new CourseOrderPublishedDto(pair.getRight().getId(),
                        pair.getLeft().getId(),
                        pair.getRight().getOrderIndex(),
                        pair.getLeft().getPrice(),
                        pair.getLeft().getTitle(),
                        null))
                .map(courseOrderPublishedDto -> {
                    int purchaseCount = courseQueryService.getPurchaseCount(courseOrderPublishedDto.courseId());
                    return courseOrderPublishedDto.addPurchaseCount(purchaseCount);
                }).collect(Collectors.toSet());
    }

    @Override
    public List<CoursePathInTrashDTO> findAllCoursePathsInTrash(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return coursePathRepository.findAllByDeleted(true, pageable).stream()
                    .map(CoursePathInTrashDTO::fromCoursePath)
                    .collect(Collectors.toList());
        }
        if (rolesBaseUtil.isTeacher()) {
            String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return coursePathRepository.findAllByTeacherAndDeleted(currentUsername, true, pageable).stream()
                    .map(CoursePathInTrashDTO::fromCoursePath)
                    .collect(Collectors.toList());
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public List<CoursePathPublishedDto> getAllCoursePathsPublishedByCourseId(Long courseId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        List<CoursePath> allPublished = coursePathRepository.findAllByPublishedAndCourseId(true, courseId, page, size);
        return allPublished.stream()
                .map(coursePath -> {
                    var courseOrders = getCourseOrderPublishedDto(coursePath);
                    return new CoursePathPublishedDto(
                            coursePath.getId(),
                            coursePath.getTitle(),
                            coursePath.getDescription(),
                            courseOrders,
                            coursePath.getTeacher(),
                            coursePath.getPublishedDate()
                    );
                }).toList();
    }

}
