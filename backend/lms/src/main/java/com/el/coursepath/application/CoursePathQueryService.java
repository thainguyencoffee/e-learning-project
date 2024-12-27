package com.el.coursepath.application;

import com.el.coursepath.application.dto.CoursePathPublishedDto;
import com.el.coursepath.domain.CoursePath;
import com.el.coursepath.web.dto.CoursePathInTrashDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CoursePathQueryService {

    CoursePath getCoursePath(Long coursePathId);

    CoursePath getCoursePath(Long coursePathId, boolean deleted);

    List<CoursePath> getCoursePaths(Pageable pageable);

    CoursePathPublishedDto getCoursePathPublishedById(Long coursePathId);

    List<CoursePathInTrashDTO> findAllCoursePathsInTrash(Pageable pageable);

    List<CoursePathPublishedDto> getAllCoursePathsPublishedByCourseId(Long courseId, Pageable pageable);
}
