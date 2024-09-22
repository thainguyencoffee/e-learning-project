package com.elearning.course.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, Long> {

    Page<Course> findAll(Pageable pageable);

    boolean existsCourseByIdAndCreatedBy(Long courseId, String createdBy);
}
