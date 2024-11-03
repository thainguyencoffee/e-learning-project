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

}
