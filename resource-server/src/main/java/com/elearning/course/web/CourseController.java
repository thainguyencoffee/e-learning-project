package com.elearning.course.web;

import com.elearning.course.application.CourseRequestDTO;
import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping(value = "/courses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> courses(Pageable pageable) {
        return ResponseEntity.ok(courseService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> course(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Long> post(@RequestBody @Valid CourseRequestDTO courseRequestDTO,
                                     @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);

        log.info("User {} attempt create course", userId);

        // if user is not admin, then automatically set teacherId to userId
        if (!jwt.getClaimAsStringList("roles").contains("admin")) {
            courseRequestDTO = CourseRequestDTO.withTeacherId(courseRequestDTO, userId);
        }

        final Course course = courseService.createCourse(courseRequestDTO);
        return new ResponseEntity<>(course.getId(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> put(@PathVariable(name = "id") final Long id,
                                    @RequestBody @Valid CourseRequestDTO courseRequestDTO,
                                    @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        log.info("User {} attempt update course id {}", userId);

        verifyRoleWithCourse(id, jwt);
        // if user is not admin, then automatically set teacherId to userId
        if (!jwt.getClaimAsStringList("roles").contains("admin")) {
            courseRequestDTO = CourseRequestDTO.withTeacherId(courseRequestDTO, userId);
        }
        courseService.updateCourse(id, courseRequestDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") final Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} attempt delete course id {}", jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME), id);

        verifyRoleWithCourse(id, jwt);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    private void verifyRoleWithCourse(final Long courseId, Jwt jwt) {
        if (!jwt.getClaimAsStringList("roles").contains("admin")) {
            if (!courseService.isItMyCourse(courseId, jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME))) {
                throw new CoursePermissionException("You don't have permission to course id: " + courseId);
            }
        }
    }

}
