package com.el.course.domain;

import com.el.course.application.dto.CourseWithoutSectionsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, Long> {

    Page<Course> findAll(Pageable pageable);

    Page<Course> findAllByDeleted(Boolean deleted, Pageable pageable);

    Optional<Course> findByIdAndDeleted(Long courseId, Boolean deleted);

    Page<Course> findAllByTeacherAndDeleted(String teacher, Boolean deleted, Pageable pageable);

    Optional<Course> findByTeacherAndIdAndDeleted(String teacher, Long courseId, Boolean deleted);

    @Query("select c.* from course c " +
            "where c.published = :published and c.deleted = :deleted LIMIT :size OFFSET :page * :size")
    List<CourseWithoutSectionsDTO> findAllByPublishedAndDeleted(Boolean published, Boolean deleted, int page, int size);

    @Query("select c.* from course c " +
            "where c.id = :courseId and c.published = :published and c.deleted = :deleted")
    Optional<CourseWithoutSectionsDTO> findPublishedCourseDTOByIdAndPublishedAndDeleted(Long courseId, Boolean published, Boolean deleted);

    Optional<Course> findByIdAndPublishedAndDeleted(Long courseId, Boolean published, Boolean deleted);

    @Query("select p.* from course c join post p on c.id = p.course " +
            "where c.id = :courseId and p.deleted = :deleted LIMIT :size OFFSET :page * :size")
    List<Post> findAllPostsByCourseIdAndDeleted(Long courseId, Boolean deleted, int page, int size);

    @Query("select p.* from course c join post p on c.id = p.course " +
            "where c.id = :courseId and c.teacher = :teacher and p.deleted = :deleted LIMIT :size OFFSET :page * :size")
    List<Post> findAllPostsByCourseIdAndTeacherAndDeleted(Long courseId, String teacher, Boolean deleted, int page, int size);

    @Query("select p.* from course c join post p on c.id = p.course " +
            "where c.id = :courseId and p.id = :postId and p.deleted = :deleted")
    Optional<Post> findPostByCourseIdAndPostIdAndDeleted(Long courseId, Long postId, Boolean deleted);

    @Query("select p.* from course c join post p on c.id = p.course " +
            "where c.id = :courseId and p.id = :postId and c.teacher = :teacher and p.deleted = :deleted")
    Optional<Post> findPostByCourseIdAndPostIdAndTeacherAndDeleted(Long courseId, Long postId, String teacher, Boolean deleted);

    @Query("select co.* from course c join post p on c.id = p.course " +
            "join comment co on co.post = p.id  " +
            "where c.id = :courseId and p.id = :postId and p.deleted = false LIMIT :size OFFSET :page * :size")
    List<Comment> findAllCommentsByCourseIdAndPostId(Long courseId, Long postId, int page, int size);

    @Query("select co.* from course c join post p on c.id = p.course " +
            "join comment co on co.post = p.id  " +
            "where c.id = :courseId and p.id = :postId and c.teacher = :teacher and p.deleted = false LIMIT :size OFFSET :page * :size")
    List<Comment> findAllCommentsByCourseIdAndPostIdAndTeacher(Long courseId, Long postId, String teacher, int page, int size);

    @Query("select q.* from course c join course_section s on c.id = s.course " +
            "join quiz q on s.id = q.course_section " +
            "where c.id = :courseId and s.id = :sectionId and c.deleted = false and q.deleted = :deleted LIMIT :size OFFSET :page * :size")
    List<Quiz> findAllQuizzesByCourseIdAndSectionIdAndDeleted(Long courseId, Long sectionId, Boolean deleted, int page, int size);

    @Query("select q.* from course c join course_section s on c.id = s.course " +
            "join quiz q on s.id = q.course_section " +
            "where c.id = :courseId and s.id = :sectionId and c.teacher = :teacher and c.deleted = false and q.deleted = :deleted " +
            "LIMIT :size OFFSET :page * :size")
    List<Quiz> findAllQuizzesByCourseIdAndSectionIdAndTeacherAndDeleted(Long courseId, Long sectionId, String teacher, Boolean deleted, int page, int size);

    @Query("select q.* from course c join course_section s on c.id = s.course " +
            "join quiz q on s.id = q.course_section " +
            "where c.id = :courseId and s.id = :sectionId and q.id = :quizId and c.deleted = false and q.deleted = :deleted")
    Optional<Quiz> findQuizByCourseIdAndSectionIdAndQuizIdAndDeleted(Long courseId, Long sectionId, Long quizId, Boolean deleted);


    @Query("select q.* from course c join course_section s on c.id = s.course " +
            "join quiz q on s.id = q.course_section " +
            "where c.id = :courseId and s.id = :sectionId and q.id = :quizId and c.teacher = :teacher and c.deleted = false and q.deleted = :deleted")
    Optional<Quiz> findQuizByCourseIdAndSectionIdAndQuizIdAndTeacherAndDeleted(Long courseId, Long sectionId, Long quizId, String teacher, Boolean deleted);

}
