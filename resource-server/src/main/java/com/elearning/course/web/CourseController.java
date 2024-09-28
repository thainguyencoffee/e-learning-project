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

    private final PublishCourseUseCase publishCourseUseCase;
    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final CourseQueryUseCase courseQueryUseCase;
    private final UpdatePriceUseCase updatePriceUseCase;
    private final AddSectionUseCase addSectionUseCase;

    public CourseController(PublishCourseUseCase publishCourseUseCase, CreateCourseUseCase createCourseUseCase, UpdateCourseUseCase updateCourseUseCase, CourseQueryUseCase courseQueryUseCase, UpdatePriceUseCase updatePriceUseCase, AddSectionUseCase addSectionUseCase) {
        this.publishCourseUseCase = publishCourseUseCase;
        this.createCourseUseCase = createCourseUseCase;
        this.updateCourseUseCase = updateCourseUseCase;
        this.courseQueryUseCase = courseQueryUseCase;
        this.updatePriceUseCase = updatePriceUseCase;
        this.addSectionUseCase = addSectionUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<Course>> courses(Pageable pageable) {
        return ResponseEntity.ok(courseQueryUseCase.findAllCourses(pageable));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> courseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseQueryUseCase.findCourseById(courseId));
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CourseDTO courseDTO) {
        String teacherId = jwt.getSubject();
        Course createdCourse = createCourseUseCase.execute(teacherId, courseDTO);
        URI location = URI.create("/api/courses/" + createdCourse.getId());
        return ResponseEntity.created(location).body(createdCourse);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
                                               @RequestBody @Valid CourseUpdateDTO courseUpdateDTO) {
        Course updatedCourse = updateCourseUseCase.execute(courseId, courseUpdateDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/publish")
    public ResponseEntity<Course> updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long courseId) {
        Course updatedCourse = publishCourseUseCase.execute(courseId, jwt.getSubject());
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/update-price")
    public ResponseEntity<Course> changePrice(@PathVariable Long courseId,
                                              @RequestBody MonetaryPriceDTO priceDTO) {
        Course updatedCourse = updatePriceUseCase.execute(courseId, priceDTO.price());
        return ResponseEntity.ok(updatedCourse);
    }

    @PostMapping("/{courseId}/sections")
    public ResponseEntity<Course> addSection(@PathVariable Long courseId,
                                             @RequestBody @Valid CourseSectionDTO courseSectionDTO) {
        Course updatedCourse = addSectionUseCase.execute(courseId, courseSectionDTO);
        return ResponseEntity.ok(updatedCourse);
    }

}