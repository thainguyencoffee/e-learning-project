package com.el.course.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, Long> {

    Page<Course> findAll(Pageable pageable);

    Page<Course> findAllByDeleted(Boolean deleted, Pageable pageable);

    Optional<Course> findByIdAndDeleted(Long courseId, Boolean deleted);

    Page<Course> findAllByTeacherAndDeleted(String teacherId, Boolean deleted, Pageable pageable);

    Optional<Course> findByTeacherAndIdAndDeleted(String teacherId, Long courseId, Boolean deleted);

    Page<Course> findAllByPublishedAndDeleted(Boolean published, Boolean deleted, Pageable pageable);

    Optional<Course> findByIdAndPublishedAndDeleted(Long courseId, Boolean published, Boolean deleted);
}
