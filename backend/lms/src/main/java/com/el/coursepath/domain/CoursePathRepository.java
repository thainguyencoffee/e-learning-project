package com.el.coursepath.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CoursePathRepository extends CrudRepository<CoursePath, Long> {

    Optional<CoursePath> findByIdAndDeleted(Long id, boolean deleted);

    Optional<CoursePath> findByIdAndTeacherAndDeleted(Long id, String teacher, boolean deleted);

    List<CoursePath> findAllByDeleted(boolean deleted, Pageable pageable);

    List<CoursePath> findAllByTeacherAndDeleted(String teacher, boolean deleted, Pageable pageable);

    Optional<CoursePath> findByIdAndPublished(Long coursePathId, boolean published);

    @Query("""
        SELECT 
            cp.*
        FROM 
            course_path cp 
        JOIN 
            course_order co ON cp.id = co.course_path
        WHERE
            cp.published = :published AND co.course_id = :courseId
        LIMIT :size OFFSET :page * :size
    """)
    List<CoursePath> findAllByPublishedAndCourseId(boolean published, Long courseId, int page, int size);
}
