package com.el.course.web;

import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.dto.CoursePostDTO;
import com.el.course.domain.Post;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/courses/{courseId}/posts", produces = "application/json")
public class CoursePostController {

    private final CourseService courseService;
    private final CourseQueryService courseQueryService;

    public CoursePostController(CourseService courseService, CourseQueryService courseQueryService) {
        this.courseService = courseService;
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(@PathVariable Long courseId, Pageable pageable) {
        List<Post> result = courseQueryService.findAllPostsByCourseId(courseId, pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping(path = "/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long courseId, @PathVariable Long postId) {
        return ResponseEntity.ok(courseQueryService.findPostByCourseIdAndPostId(courseId, postId));
    }

    @PostMapping
    public ResponseEntity<Long> createPost(@PathVariable Long courseId, @Valid @RequestBody CoursePostDTO coursePostDTO) {
        return ResponseEntity.ok(courseService.addPost(courseId, coursePostDTO));
    }

    @PutMapping(path = "/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long courseId, @PathVariable Long postId, @Valid @RequestBody CoursePostDTO coursePostDTO) {
        courseService.updatePost(courseId, postId, coursePostDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long courseId, @PathVariable Long postId, @RequestParam(required = false) boolean force) {
        if (!force) {
            courseService.deletePost(courseId, postId);
        } else {
            courseService.deleteForcePost(courseId, postId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{postId}/restore")
    public ResponseEntity<Void> restorePost(@PathVariable Long courseId, @PathVariable Long postId) {
        courseService.restorePost(courseId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<Page<Post>> getTrashedPost(@PathVariable Long courseId, Pageable pageable) {
        List<Post> result = courseQueryService.findTrashedPosts(courseId, pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

}
