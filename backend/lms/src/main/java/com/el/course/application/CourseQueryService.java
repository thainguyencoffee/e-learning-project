package com.el.course.application;

import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseQueryService {

    Page<Course> findAllCourses(Pageable pageable);

    Page<Course> findTrashedCourses(Pageable pageable);

    Course findCourseById(Long courseId);

    Course findCourseInTrashById(Long courseId);

    Course findCourseDeleted(Long courseId);

    Course findPublishedCourseById(Long courseId);

    List<CourseWithoutSectionsDTO> findAllCourseWithoutSectionsDTOs(Pageable pageable);

    CourseWithoutSectionsDTO findCourseWithoutSectionsDTOById(Long courseId);

}
