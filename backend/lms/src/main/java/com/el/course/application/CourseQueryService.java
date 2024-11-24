package com.el.course.application;

import com.el.course.application.dto.CourseInTrashDTO;
import com.el.course.application.dto.PostInTrashDTO;
import com.el.course.application.dto.PublishedCourseDTO;
import com.el.course.application.dto.QuizInTrashDTO;
import com.el.course.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseQueryService {

    Page<Course> findAllCourses(Pageable pageable);

    Course findCourseById(Long courseId, Boolean deleted);

    List<CourseInTrashDTO> findAllCoursesInTrash(Pageable pageable);

    Course findPublishedCourseById(Long courseId);

    Page<Course> findAllPublishedCourses(Pageable pageable);

    List<PublishedCourseDTO> findAllPublishedCoursesDTO(Pageable pageable);

    PublishedCourseDTO findCoursePublishedById(Long courseId);

    List<Post> findAllPostsByCourseId(Long courseId, Pageable pageable);

    Post findPostByCourseIdAndPostId(Long courseId, Long postId);

    List<PostInTrashDTO> findAllPostsInTrash(Long courseId, Pageable pageable);

    List<Comment> findCommentsByPostId(Long courseId, Long postId, Pageable pageable);

    List<Quiz> findAllQuizzes(Long courseId, Long sectionId, Pageable pageable);

    Quiz findQuizById(Long courseId, Long sectionId, Long quizId);

    List<QuizInTrashDTO> findAllQuizzesInTrash(Long courseId, Long sectionId, Pageable pageable);

    Quiz findQuizByQuizId(Long quizId);

    Lesson findLessonByCourseIdAndLessonId(Long courseId, Long lessonId);

    List<PublishedCourseDTO> searchPublishedCoursesDTO(String query, Pageable pageable);
}
