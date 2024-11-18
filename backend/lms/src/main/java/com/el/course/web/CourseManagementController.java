package com.el.course.web;

import com.el.course.application.*;
import com.el.course.application.dto.*;
import com.el.course.domain.Course;
import com.el.course.domain.RequestType;
import com.el.course.web.dto.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/courses")
public class CourseManagementController {

    private final CourseService courseService;
    private final CourseQueryService courseQueryService;

    public CourseManagementController(CourseService courseService, CourseQueryService courseQueryService) {
        this.courseService = courseService;
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<Course>> getAllCourses(Pageable pageable) {
        return ResponseEntity.ok(courseQueryService.findAllCourses(pageable));
    }

    @GetMapping("/trash")
    public ResponseEntity<Page<Course>> getTrashedCourses(Pageable pageable) {
        return ResponseEntity.ok(courseQueryService.findTrashedCourses(pageable));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseQueryService.findCourseById(courseId));
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CourseDTO courseDTO
    ) {
        String teacher = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
        Course createdCourse = courseService.createCourse(teacher, courseDTO);
        URI location = URI.create("/courses/" + createdCourse.getId());
        return ResponseEntity.created(location).body(createdCourse);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
                                               @RequestBody @Valid CourseUpdateDTO courseUpdateDTO) {
        Course updatedCourse = courseService.updateCourse(courseId, courseUpdateDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId, @RequestParam(required = false) boolean force) {
        if (!force) {
            courseService.deleteCourse(courseId);
        } else {
            courseService.deleteCourseForce(courseId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/restore")
    public ResponseEntity<Void> restoreCourse(@PathVariable Long courseId) {
        courseService.restoreCourse(courseId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{courseId}/requests")
    public ResponseEntity<Course> requestPublish(@PathVariable Long courseId,
                                                 @Valid @RequestBody CourseRequestDTO courseRequestDTO) {
        if (courseRequestDTO.type() == RequestType.PUBLISH) {
            courseService.requestPublish(courseId, courseRequestDTO);
        } else {
            courseService.requestUnpublish(courseId, courseRequestDTO);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{courseId}/requests/{requestId}/approve")
    public ResponseEntity<Course> approvePublish(@PathVariable Long courseId, @PathVariable Long requestId, @Valid @RequestBody CourseRequestApproveDTO courseRequestApproveDTO) {
        if (courseRequestApproveDTO.approveType() == RequestType.PUBLISH) {
            courseService.approvePublish(courseId, requestId, courseRequestApproveDTO.toCourseRequestResolveDTO());
        } else {
            courseService.approveUnpublish(courseId, requestId, courseRequestApproveDTO.toCourseRequestResolveDTO());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{courseId}/requests/{requestId}/reject")
    public ResponseEntity<Course> rejectPublish(@PathVariable Long courseId, @PathVariable Long requestId, @Valid @RequestBody CourseRequestRejectDTO courseRequestRejectDTO) {
        if (courseRequestRejectDTO.rejectType() == RequestType.PUBLISH) {
            courseService.rejectPublish(courseId, requestId, courseRequestRejectDTO.toCourseRequestResolveDTO());
        } else {
            courseService.rejectUnpublish(courseId, requestId, courseRequestRejectDTO.toCourseRequestResolveDTO());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{courseId}/update-price")
    public ResponseEntity<Course> changePrice(@PathVariable Long courseId,
                                              @Valid @RequestBody UpdatePriceDTO priceDTO) {
        Course updatedCourse = courseService.updatePrice(courseId, priceDTO.price());
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/assign-teacher")
    public ResponseEntity<Course> assignTeacher(@PathVariable Long courseId,
                                                @Valid @RequestBody AssignTeacherDTO assignTeacherDTO) {
        return ResponseEntity.ok(courseService.assignTeacher(courseId, assignTeacherDTO.teacher()));
    }

    @PostMapping("/{courseId}/sections")
    public ResponseEntity<Long> addSection(@PathVariable Long courseId,
                                             @RequestBody @Valid CourseSectionDTO courseSectionDTO) {
        Long sectionId = courseService.addSection(courseId, courseSectionDTO);
        return ResponseEntity.ok(sectionId);
    }

    @PutMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<Void> updateSectionInfo(@PathVariable Long courseId,
                                                    @PathVariable Long sectionId,
                                                    @RequestBody @Valid UpdateSectionDTO updateSectionDTO) {
        courseService.updateSectionInfo(courseId, sectionId, updateSectionDTO.title());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long courseId, @PathVariable Long sectionId) {
        courseService.removeSection(courseId, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<Long> addLesson(@PathVariable Long courseId,
                                            @PathVariable Long sectionId,
                                            @RequestBody @Valid LessonDTO lessonDTO) {
        Long lessonId = courseService.addLesson(courseId, sectionId, lessonDTO.toLesson());
        return ResponseEntity.ok(lessonId);
    }

    @PutMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<Void> updateLesson(@PathVariable Long courseId,
                                               @PathVariable Long sectionId,
                                               @PathVariable Long lessonId,
                                               @RequestBody @Valid LessonDTO lessonDTO) {
        courseService.updateLesson(courseId, sectionId, lessonId, lessonDTO.toLesson());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId,
                                               @PathVariable Long sectionId,
                                               @PathVariable Long lessonId) {
        courseService.removeLesson(courseId, sectionId, lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<Long> addReview(@PathVariable Long courseId,
                                          @RequestParam("enrollmentId") Long enrollmentId,
                                          @RequestBody @Valid ReviewDTO reviewDTO) {
        log.info("Add review for course: " + courseId + " with enrollment: " + enrollmentId);
        return ResponseEntity.ok(courseService.addReview(courseId, enrollmentId, reviewDTO));
    }

}