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

}
