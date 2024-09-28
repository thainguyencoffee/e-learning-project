package com.elearning.course.web;

import com.elearning.course.application.*;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<Page<Course>> courses(Pageable pageable) {
        return ResponseEntity.ok(courseService.findAllCourses(pageable));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> courseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.findCourseById(courseId));
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CourseDTO courseDTO
    ) {
        String teacherId = jwt.getSubject();
        Course createdCourse = courseService.createCourse(teacherId, courseDTO);
        URI location = URI.create("/courses/" + createdCourse.getId());
        return ResponseEntity.created(location).body(createdCourse);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
                                               @RequestBody @Valid CourseUpdateDTO courseUpdateDTO) {
        Course updatedCourse = courseService.updateCourse(courseId, courseUpdateDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/publish")
    public ResponseEntity<Course> updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long courseId) {
        Course updatedCourse = courseService.publishCourse(courseId, jwt.getSubject());
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/update-price")
    public ResponseEntity<Course> changePrice(@PathVariable Long courseId,
                                              @RequestBody MonetaryPriceDTO priceDTO) {
        Course updatedCourse = courseService.updatePrice(courseId, priceDTO.price());
        return ResponseEntity.ok(updatedCourse);
    }

    @PostMapping("/{courseId}/sections")
    public ResponseEntity<Course> addSection(@PathVariable Long courseId,
                                             @RequestBody @Valid CourseSectionDTO courseSectionDTO) {
        Course updatedCourse = courseService.addSection(courseId, courseSectionDTO);
        return ResponseEntity.ok(updatedCourse);
    }


}