package com.elearning.course.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, Long> {

    Page<Course> findAll(Pageable pageable);

    Optional<Course> findByIdAndDeleted(Long courseId, Boolean deleted);

    Page<Course> findAllByPublished(Boolean published, Pageable pageable);

    Optional<Course> findByIdAndPublished(Long courseId, Boolean published);
}
