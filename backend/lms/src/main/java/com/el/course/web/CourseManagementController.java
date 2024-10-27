package com.el.course.web;

import com.el.course.application.*;
import com.el.course.application.dto.*;
import com.el.course.domain.Course;
import com.el.course.domain.RequestType;
import com.el.course.web.dto.AssignTeacherDTO;
import com.el.course.web.dto.UpdatePriceDTO;
import com.el.course.web.dto.UpdateSectionDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public ResponseEntity<Course> addSection(@PathVariable Long courseId,
                                             @RequestBody @Valid CourseSectionDTO courseSectionDTO) {
        Course updatedCourse = courseService.addSection(courseId, courseSectionDTO);
        return ResponseEntity.created(URI.create("/courses/" + courseId)).body(updatedCourse);
    }

    @PutMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<Course> updateSectionInfo(@PathVariable Long courseId,
                                                    @PathVariable Long sectionId,
                                                    @RequestBody @Valid UpdateSectionDTO updateSectionDTO) {
        Course updatedCourse = courseService.updateSectionInfo(courseId, sectionId, updateSectionDTO.title());
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long courseId, @PathVariable Long sectionId) {
        courseService.removeSection(courseId, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<Course> addLesson(@PathVariable Long courseId,
                                            @PathVariable Long sectionId,
                                            @RequestBody @Valid LessonDTO lessonDTO) {
        Course updatedCourse = courseService.addLesson(courseId, sectionId, lessonDTO.toLesson());
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<Course> updateLesson(@PathVariable Long courseId,
                                               @PathVariable Long sectionId,
                                               @PathVariable Long lessonId,
                                               @RequestBody @Valid LessonDTO lessonDTO) {
        Course updatedCourse = courseService.updateLesson(courseId, sectionId, lessonId, lessonDTO.toLesson());
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId,
                                               @PathVariable Long sectionId,
                                               @PathVariable Long lessonId) {
        courseService.removeLesson(courseId, sectionId, lessonId);
        return ResponseEntity.noContent().build();
    }

}