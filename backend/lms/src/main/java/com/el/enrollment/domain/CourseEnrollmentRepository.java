package com.el.enrollment.domain;

import com.el.enrollment.application.CourseEnrollmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends CrudRepository<CourseEnrollment, Long> {

    Page<CourseEnrollment> findAllByCourseId(Long courseId, Pageable pageable);

    Integer countAllByCourseId(Long courseId);

    Page<CourseEnrollment> findAll(Pageable pageable);

    @Query("select ce.id, ce.student, ce.course_id, c.title, c.thumbnail_url, c.teacher, ce.enrollment_date, ce.completed " +
            "from course_enrollment ce join course c on ce.course_id = c.id where ce.student = :student LIMIT :size OFFSET :page * :size")
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOsByStudent(String student, int page, int size);

    @Query("select ce.id, ce.student, ce.course_id, c.title, c.thumbnail_url, c.teacher, ce.enrollment_date, ce.completed " +
            "from course_enrollment ce join course c on ce.course_id = c.id LIMIT :size OFFSET :page * :size")
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOs(int page, int size);

    Page<CourseEnrollment> findAllByStudent(String student, Pageable pageable);

    Optional<CourseEnrollment> findByIdAndStudent(Long id, String student);

}
