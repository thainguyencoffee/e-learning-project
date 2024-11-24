package com.el.enrollment.domain;

import com.el.common.projection.MonthStats;
import com.el.course.application.dto.teacher.StudentsByCourseDTO;
import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrollmentStatisticDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends CrudRepository<Enrollment, Long> {

    Integer countAllByCourseId(Long courseId);

    @Query("""
        SELECT 
            e.id, 
            e.student, 
            e.course_id, 
            c.title, 
            c.thumbnail_url, 
            c.teacher, 
            e.enrollment_date, 
            e.completed
        FROM enrollment e
        JOIN course c on e.course_id = c.id
        WHERE e.student = :student
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOsByStudent(String student, int page, int size);

    @Query("""
        SELECT 
            e.id, 
            e.student, 
            e.course_id, 
            c.title, 
            c.thumbnail_url, 
            c.teacher, 
            e.enrollment_date, 
            e.completed
        FROM 
            enrollment e
        JOIN course c on e.course_id = c.id
        WHERE c.teacher = :teacher
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOsByTeacher(String teacher, int page, int size);

    @Query("""
        SELECT 
            e.id, 
            e.student, 
            e.course_id, 
            c.title, 
            c.thumbnail_url, 
            c.teacher, 
            e.enrollment_date, 
            e.completed 
        FROM 
            enrollment e 
        JOIN 
            course c on e.course_id = c.id 
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOs(int page, int size);

    Optional<Enrollment> findByIdAndStudent(Long id, String student);

    @Query("""
        SELECT
            e.*
        FROM
            enrollment e
                JOIN
            course c ON e.course_id = c.id
        WHERE e.id = :id 
          AND c.teacher = :teacher
    """)
    Optional<Enrollment> findByIdAndTeacher(Long id, String teacher);

    Optional<Enrollment> findByCourseIdAndStudent(Long courseId, String student);

    List<Enrollment> findAllByCourseId(long courseId);

    @Query("""
        SELECT
            c.id AS course_id,
            c.title AS title,
            c.thumbnail_url AS thumbnail_url,
            c.description AS description,
            c.teacher AS teacher,
            COUNT(e.id) AS total_enrollments,
            SUM(CASE WHEN e.completed = true THEN 1 ELSE 0 END) AS total_completed_enrollments
        FROM
            course c
                JOIN
            enrollment e ON c.id = e.course_id
        WHERE
            c.published = true
        GROUP BY
            c.id, c.title, c.thumbnail_url, c.description, c.teacher 
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseInfoWithEnrollmentStatisticDTO> findAllCourseStatistics(int page, int size);

    @Query("""
        SELECT
            c.id AS course_id,
            c.title AS title,
            c.thumbnail_url AS thumbnail_url,
            c.description AS description,
            c.teacher AS teacher,
            COUNT(e.id) AS total_enrollments,
            SUM(CASE WHEN e.completed = true THEN 1 ELSE 0 END) AS total_completed_enrollments
        FROM
            course c
                JOIN
            enrollment e ON c.id = e.course_id
        WHERE
            c.published = true AND c.teacher = :teacher
        GROUP BY
            c.id, c.title, c.thumbnail_url, c.description, c.teacher 
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseInfoWithEnrollmentStatisticDTO> findAllCourseStatisticsByTeacher(String teacher, int page, int size);

    int countCourseEnrollmentByTeacherAndCreatedDateAfter(String teacher, LocalDateTime createdDateAfter);

    @Query("""
        SELECT 
            extract(MONTH from e.enrollment_date) as month, COUNT(e.id) AS count
        FROM 
            enrollment e
        JOIN course c on e.course_id = c.id
        WHERE c.teacher = :teacher 
          and extract(YEAR from e.enrollment_date) = :year
        GROUP BY extract(MONTH from e.enrollment_date)
    """)
    List<MonthStats> statsMonthEnrolledByTeacherAndYear(String teacher, Integer year);

    @Query("""
        SELECT
            c.id,
            c.title,
            c.thumbnail_url,
            COUNT(e.student) AS total_students,
            COUNT(CASE WHEN e.completed THEN 1 END) AS completed_students
        FROM course c
        JOIN enrollment e ON c.id = e.course_id
        WHERE c.teacher = :teacher
        GROUP BY
            c.id, c.title, c.thumbnail_url
        ORDER BY
            completed_students DESC
        LIMIT :size OFFSET :page * :size
    """)
    List<StudentsByCourseDTO> statsStudentsByCourse(String teacher, int page, int size);

}
