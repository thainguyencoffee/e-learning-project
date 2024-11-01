package com.el.course.web;

import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.CourseWithoutSectionsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/published-courses")
public class BrowseCourseController {

    private final CourseQueryService courseQueryService;

    public BrowseCourseController(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<CourseWithoutSectionsDTO>> getAllPublishedCourses(Pageable pageable) {
        List<CourseWithoutSectionsDTO> result = courseQueryService.findAllCourseWithoutSectionsDTOs(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseWithoutSectionsDTO> getPublishedCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseQueryService.findCourseWithoutSectionsDTOById(courseId));
    }

}
