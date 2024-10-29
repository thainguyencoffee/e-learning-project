package com.el.enrollment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourseEnrollmentRepository extends CrudRepository<CourseEnrollment, Long> {

    Page<CourseEnrollment> findAll(Pageable pageable);

    Page<CourseEnrollment> findAllByStudent(String student, Pageable pageable);

    Optional<CourseEnrollment> findByIdAndStudent(Long id, String student);

}
