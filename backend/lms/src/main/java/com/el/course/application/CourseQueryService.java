package com.el.course.application;

import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.domain.Comment;
import com.el.course.domain.Course;
import com.el.course.domain.Post;
import com.el.course.domain.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseQueryService {

    Page<Course> findAllCourses(Pageable pageable);

    Page<Course> findTrashedCourses(Pageable pageable);

    Course findCourseById(Long courseId);

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

}
