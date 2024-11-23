package com.el.course.application;

import com.el.course.application.dto.CourseInTrashDTO;
import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseQueryService {

    Page<Course> findAllCourses(Pageable pageable);

    Course findCourseById(Long courseId);

    List<CourseInTrashDTO> findAllCoursesInTrash(Pageable pageable);

    Course findCourseInTrashById(Long courseId);

    Course findCourseDeleted(Long courseId);

    Course findPublishedCourseById(Long courseId);

    Page<Course> findAllPublishedCourses(Pageable pageable);

    List<CourseWithoutSectionsDTO> findAllCourseWithoutSectionsDTOs(Pageable pageable);

    CourseWithoutSectionsDTO findCourseWithoutSectionsDTOById(Long courseId);

    List<Post> findAllPostsByCourseId(Long courseId, Pageable pageable);

    Post findPostByCourseIdAndPostId(Long courseId, Long postId);

    List<Post> findTrashedPosts(Long courseId, Pageable pageable);

    List<Comment> findCommentsByPostId(Long courseId, Long postId, Pageable pageable);

    List<Quiz> findQuizzesByCourseIdAndSectionId(Long courseId, Long sectionId, Pageable pageable);

    Quiz findQuizByCourseIdAndSectionIdAndQuizId(Long courseId, Long sectionId, Long quizId);

    List<Quiz> findTrashQuizzesByCourseIdAndSectionId(Long courseId, Long sectionId, Pageable pageable);

    Quiz findQuizByQuizId(Long quizId);

    Lesson findLessonByCourseIdAndLessonId(Long courseId, Long lessonId);
}
