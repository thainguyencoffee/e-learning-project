package com.elearning.course.web;

import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BrowseCourseController {

    private final CourseService courseService;

    public BrowseCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/published-courses")
    public ResponseEntity<Page<Course>> browseCourses(Pageable pageable) {
        return ResponseEntity.ok(courseService.findAllPublishedCourses(pageable));
    }

    @GetMapping("/published-courses/{courseId}")
    public ResponseEntity<Course> browseCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.findPublishedCourseById(courseId));
    }

}
