package com.el.coursepath.web;

import com.el.coursepath.application.CoursePathQueryService;
import com.el.coursepath.application.dto.CoursePathPublishedDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "course-paths-published", produces = "application/json")
public class CoursePathPublishedController {

    private final CoursePathQueryService coursePathQueryService;

    public CoursePathPublishedController(CoursePathQueryService coursePathQueryService) {
        this.coursePathQueryService = coursePathQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<CoursePathPublishedDto>> getPublishedCoursePaths(Pageable pageable, @RequestParam Long courseId) {
        List<CoursePathPublishedDto> coursePathsPublished = coursePathQueryService.getAllCoursePathsPublishedByCourseId(courseId, pageable);
        return ResponseEntity.ok(new PageImpl<>(coursePathsPublished, pageable, coursePathsPublished.size()));
    }

    @GetMapping("/{coursePathId}")
    public ResponseEntity<CoursePathPublishedDto> getPublishedCoursePathById(@PathVariable Long coursePathId) {
        CoursePathPublishedDto coursePath = coursePathQueryService.getCoursePathPublishedById(coursePathId);
        return ResponseEntity.ok(coursePath);
    }

}
