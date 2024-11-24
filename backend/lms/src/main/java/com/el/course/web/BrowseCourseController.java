package com.el.course.web;

import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.PublishedCourseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/published-courses")
public class BrowseCourseController {

    private final CourseQueryService courseQueryService;

    public BrowseCourseController(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<PublishedCourseDTO>> getAllPublishedCourses(Pageable pageable) {
        List<PublishedCourseDTO> result = courseQueryService.findAllPublishedCoursesDTO(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<PublishedCourseDTO> getPublishedCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseQueryService.findCoursePublishedById(courseId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PublishedCourseDTO>> searchPublishedCourse(@RequestParam String query, Pageable pageable) {
        List<PublishedCourseDTO> result = courseQueryService.searchPublishedCoursesDTO(query, pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

}
