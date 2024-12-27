package com.el.coursepath.web;

import com.el.coursepath.application.CoursePathQueryService;
import com.el.coursepath.application.CoursePathService;
import com.el.coursepath.domain.CoursePath;
import com.el.coursepath.web.dto.CourseOrderDTO;
import com.el.coursepath.web.dto.CoursePathDTO;
import com.el.coursepath.web.dto.CoursePathInTrashDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "course-paths", produces = "application/json")
public class CoursePathController {

    private final CoursePathService coursePathService;
    private final CoursePathQueryService coursePathQueryService;

    public CoursePathController(CoursePathService coursePathService, CoursePathQueryService coursePathQueryService) {
        this.coursePathService = coursePathService;
        this.coursePathQueryService = coursePathQueryService;
    }

    @GetMapping("/trash")
    public ResponseEntity<Page<CoursePathInTrashDTO>> getTrashedCoursePaths(Pageable pageable) {
        List<CoursePathInTrashDTO> result = coursePathQueryService.findAllCoursePathsInTrash(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{coursePathId}")
    public ResponseEntity<CoursePath> getCoursePath(@PathVariable Long coursePathId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        return ResponseEntity.ok(coursePath);
    }

    @GetMapping
    public ResponseEntity<Page<CoursePath>> getCoursePaths(Pageable pageable) {
        List<CoursePath> coursePaths = coursePathQueryService.getCoursePaths(pageable);
        return ResponseEntity.ok(new PageImpl<>(coursePaths, pageable, coursePaths.size()));
    }

    @PostMapping
    public ResponseEntity<Long> createCoursePath(@Valid @RequestBody CoursePathDTO coursePathDTO) {
        Long coursePathId = coursePathService.createCoursePath(coursePathDTO);
        return ResponseEntity.created(URI.create("/course-paths/" + coursePathId)).body(coursePathId);
    }

    @PutMapping("/{coursePathId}")
    public ResponseEntity<Void> updateCoursePath(@PathVariable Long coursePathId,
                                                 @Valid @RequestBody CoursePathDTO coursePathDTO) {
        coursePathService.updateCoursePath(coursePathId, coursePathDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{coursePathId}")
    public ResponseEntity<Void> deleteCoursePath(@PathVariable Long coursePathId,
                                                 @RequestParam(required = false) boolean force) {
        if (!force) {
            coursePathService.deleteCoursePath(coursePathId);
        } else {
            coursePathService.deleteCoursePathForce(coursePathId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{coursePathId}/restore")
    public ResponseEntity<Void> restoreCoursePath(@PathVariable Long coursePathId) {
        coursePathService.restoreCoursePath(coursePathId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{coursePathId}/courseOrders")
    public ResponseEntity<Long> addCourseOrder(@PathVariable Long coursePathId,
                                               @Valid @RequestBody CourseOrderDTO courseOrderDTO) {
        Long courseOrderId = coursePathService.addCourseOrder(coursePathId, courseOrderDTO.courseId());
        return new ResponseEntity<>(courseOrderId, HttpStatus.CREATED);
    }

    @DeleteMapping("/{coursePathId}/courseOrders/{courseOrderId}")
    public ResponseEntity<Void> removeCourseOrder(@PathVariable Long coursePathId,
                                                  @PathVariable Long courseOrderId) {
        coursePathService.removeCourseOrder(coursePathId, courseOrderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{coursePathId}/publish")
    public ResponseEntity<Void> publishCoursePath(@PathVariable Long coursePathId) {
        coursePathService.publishCoursePath(coursePathId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{coursePathId}/unpublish")
    public ResponseEntity<Void> unpublishCoursePath(@PathVariable Long coursePathId) {
        coursePathService.unpublishCoursePath(coursePathId);
        return ResponseEntity.ok().build();
    }

}
