package com.elearning.course.web;

import com.elearning.course.application.CourseQueryService;
import com.elearning.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/published-courses")
public class BrowseCourseController {

    private final CourseQueryService courseQueryService;

    public BrowseCourseController(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<Course>> getAllPublishedCourses(Pageable pageable) {
        return ResponseEntity.ok(courseQueryService.findAllPublishedCourses(pageable));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getPublishedCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseQueryService.findPublishedCourseById(courseId));
    }

}
