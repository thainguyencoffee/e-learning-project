package com.elearning.course.web;

import com.elearning.course.application.CourseRequestDTO;
import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
    public ResponseEntity<Long> post(@RequestBody @Valid final CourseRequestDTO courseRequestDTO) {
        final Long createdId = courseService.createCourse(courseRequestDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> put(@PathVariable(name = "id") final Long id,
                                    @RequestBody @Valid final CourseRequestDTO courseRequestDTO) {
        courseService.updateCourse(id, courseRequestDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") final Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

}
