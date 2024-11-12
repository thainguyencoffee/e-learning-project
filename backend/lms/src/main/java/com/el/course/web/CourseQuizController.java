package com.el.course.web;

import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.dto.QuestionDTO;
import com.el.course.application.dto.QuizDTO;
import com.el.course.application.dto.QuizUpdateDTO;
import com.el.course.domain.Quiz;
import com.el.course.web.dto.QuizSubmitDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/courses/{courseId}/sections/{sectionId}/quizzes", produces = "application/json")
public class CourseQuizController {

    private final CourseService courseService;
    private final CourseQueryService courseQueryService;

    public CourseQuizController(CourseService courseService, CourseQueryService courseQueryService) {
        this.courseService = courseService;
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<Quiz>> getAllQuizzes(@PathVariable Long courseId, @PathVariable Long sectionId, Pageable pageable) {
        List<Quiz> result = courseQueryService.findQuizzesByCourseIdAndSectionId(courseId, sectionId, pageable);
        return ResponseEntity.ok().body(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long courseId, @PathVariable Long sectionId, @PathVariable Long quizId) {
        return ResponseEntity.ok(courseQueryService.findQuizByCourseIdAndSectionIdAndQuizId(courseId, sectionId, quizId));
    }

    @GetMapping("/trash")
    public ResponseEntity<Page<Quiz>> getDeletedQuizzes(@PathVariable Long courseId, @PathVariable Long sectionId, Pageable pageable) {
        List<Quiz> result = courseQueryService.findTrashQuizzesByCourseIdAndSectionId(courseId, sectionId, pageable);
        return ResponseEntity.ok().body(new PageImpl<>(result, pageable, result.size()));
    }

    @PostMapping
    public ResponseEntity<Long> addQuiz(@PathVariable Long courseId, @PathVariable Long sectionId,
                                        @Valid @RequestBody QuizDTO quizDTO) {
        return ResponseEntity.ok(courseService.addQuizToSection(courseId, sectionId, quizDTO));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<Void> updateQuiz(@PathVariable Long courseId, @PathVariable Long sectionId,
                                           @PathVariable Long quizId, @Valid @RequestBody QuizUpdateDTO quizUpdateDTO) {
        courseService.updateQuiz(courseId, sectionId, quizId, quizUpdateDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long courseId,
                                           @PathVariable Long sectionId,
                                           @PathVariable Long quizId,
                                           @RequestParam(required = false, defaultValue = "false") boolean force) {

        if (!force) {
            courseService.deleteQuiz(courseId, sectionId, quizId);
        } else {
            courseService.deleteForceQuiz(courseId, sectionId, quizId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/restore")
    public ResponseEntity<Void> restoreQuiz(@PathVariable Long courseId, @PathVariable Long sectionId, @PathVariable Long quizId) {
        courseService.restoreQuiz(courseId, sectionId, quizId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<Long> addQuestion(@PathVariable Long courseId, @PathVariable Long sectionId,
                                            @PathVariable Long quizId, @Valid @RequestBody QuestionDTO questionDTO) {
        return ResponseEntity.ok(courseService.addQuestionToQuiz(courseId, sectionId, quizId, questionDTO));
    }

    @PutMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> updateQuestion(@PathVariable Long courseId, @PathVariable Long sectionId,
                                               @PathVariable Long quizId, @PathVariable Long questionId,
                                               @Valid @RequestBody QuestionDTO questionUpdateDTO) {
        courseService.updateQuestion(courseId, sectionId, quizId, questionId, questionUpdateDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long courseId, @PathVariable Long sectionId,
                                               @PathVariable Long quizId, @PathVariable Long questionId) {
        courseService.deleteQuestion(courseId, sectionId, quizId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<Void> submitQuiz(@PathVariable Long courseId,
                                           @PathVariable Long sectionId,
                                           @PathVariable Long quizId,
                                           @Valid @RequestBody QuizSubmitDTO quizSubmitDTO) {
        courseService.submitQuiz(courseId, sectionId, quizId, quizSubmitDTO);
        return ResponseEntity.ok().build();
    }

}
