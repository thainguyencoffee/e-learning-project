package com.el.enrollment.domain;

import com.el.common.projection.MonthStats;
import com.el.course.domain.StudentsByCourseDTO;
import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentStatisticDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends CrudRepository<CourseEnrollment, Long> {

    Integer countAllByCourseId(Long courseId);

    @Query("select ce.id, ce.student, ce.course_id, c.title, c.thumbnail_url, c.teacher, ce.enrollment_date, ce.completed " +
            "from course_enrollment ce join course c on ce.course_id = c.id where ce.student = :student LIMIT :size OFFSET :page * :size")
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOsByStudent(String student, int page, int size);

    @Query("""
    select ce.id, ce.student, ce.course_id, c.title, c.thumbnail_url, c.teacher, ce.enrollment_date, ce.completed
        from course_enrollment ce
        join course c on ce.course_id = c.id
        where c.teacher = :teacher
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOsByTeacher(String teacher, int page, int size);

    @Query("select ce.id, ce.student, ce.course_id, c.title, c.thumbnail_url, c.teacher, ce.enrollment_date, ce.completed " +
            "from course_enrollment ce join course c on ce.course_id = c.id LIMIT :size OFFSET :page * :size")
    List<CourseEnrollmentDTO> findAllCourseEnrollmentDTOs(int page, int size);

    Optional<CourseEnrollment> findByIdAndStudent(Long id, String student);

    @Query("""
        SELECT
            ce.*
        FROM
            course_enrollment ce
                JOIN
            course c ON ce.course_id = c.id
        WHERE
            ce.id = :id AND c.teacher = :teacher
    """)
    Optional<CourseEnrollment> findByIdAndTeacher(Long id, String teacher);

    Optional<CourseEnrollment> findByCourseIdAndStudent(Long courseId, String student);

    List<CourseEnrollment> findAllByCourseId(long courseId);

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
            course_enrollment e ON c.id = e.course_id
        WHERE
            c.published = true
        GROUP BY
            c.id, c.title, c.thumbnail_url, c.description, c.teacher LIMIT :size OFFSET :page * :size
    """)
    List<CourseInfoWithEnrolmentStatisticDTO> findAllCourseStatistics(int page, int size);

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
            course_enrollment e ON c.id = e.course_id
        WHERE
            c.published = true AND c.teacher = :teacher
        GROUP BY
            c.id, c.title, c.thumbnail_url, c.description, c.teacher LIMIT :size OFFSET :page * :size
    """)
    List<CourseInfoWithEnrolmentStatisticDTO> findAllCourseStatisticsByTeacher(String teacher, int page, int size);

    int countCourseEnrollmentByTeacherAndCreatedDateAfter(String teacher, LocalDateTime createdDateAfter);

    @Query("""
        select extract(MONTH from ce.enrollment_date) as month, COUNT(ce.id) AS count
        from course_enrollment ce
            join course c on ce.course_id = c.id
        where c.teacher = :teacher and extract(YEAR from ce.enrollment_date) = :year
        group by extract(MONTH from ce.enrollment_date)
    """)
    List<MonthStats> statsMonthEnrolledByTeacherAndYear(String teacher, Integer year);

    @Query("""
        SELECT
            c.id,
            c.title,
            c.thumbnail_url,
            COUNT(ce.student) AS total_students,
            COUNT(CASE WHEN ce.completed THEN 1 END) AS completed_students
        FROM course c
            JOIN course_enrollment ce ON c.id = ce.course_id
        WHERE c.teacher = :teacher
        GROUP BY
            c.id, c.title, c.thumbnail_url
        ORDER BY
            completed_students DESC
        LIMIT :size OFFSET :page * :size
    """)
    List<StudentsByCourseDTO> statsStudentsByCourse(String teacher, int page, int size);

}
