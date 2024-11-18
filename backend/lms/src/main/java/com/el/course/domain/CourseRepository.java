package com.el.course.domain;

import com.el.common.projection.MonthStats;
import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.application.dto.teacher.CountDataDTO;
import com.el.enrollment.application.dto.CourseInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, Long> {

    @Query("""
        SELECT 
            c.teacher,
            COUNT(DISTINCT CASE WHEN c.published = TRUE THEN 1 END) as number_of_courses,
            COUNT(e.id) as number_of_students,
            COUNT(CASE when e.completed = TRUE THEN 1 END) as number_of_certificates,
            COUNT(DISTINCT CASE WHEN c.published = FALSE THEN 1 END) as number_of_draft_courses
        FROM 
            course c
        LEFT JOIN 
            course_enrollment e ON c.id = e.course_id
        WHERE
            c.teacher = :teacher
        GROUP BY 
            c.teacher
    """)
    CountDataDTO getCountDataDTOByTeacher(String teacher);

    Page<Course> findAll(Pageable pageable);

    Page<Course> findAllByDeleted(Boolean deleted, Pageable pageable);

    Optional<Course> findByIdAndDeleted(Long courseId, Boolean deleted);

    Page<Course> findAllByTeacherAndDeleted(String teacher, Boolean deleted, Pageable pageable);

    Optional<Course> findByTeacherAndIdAndDeleted(String teacher, Long courseId, Boolean deleted);

    @Query("""
        SELECT c.id, c.title, c.thumbnail_url, c.description, c.language, c.subtitles, c.benefits, c.prerequisites, c.price, c.teacher
            FROM course c
            WHERE c.published = :published
                AND c.deleted = :deleted
    """)
    List<CourseWithoutSectionsDTO> findAllByPublishedAndDeleted(Boolean published, Boolean deleted, int page, int size);

    @Query("""
        SELECT c.id, c.title, c.thumbnail_url, c.description, c.language, c.subtitles, c.benefits, c.prerequisites, c.price, c.teacher
            FROM course c
            WHERE c.id = :courseId
                AND c.published = :published
                AND c.deleted = :deleted
    """)
    Optional<CourseWithoutSectionsDTO> findCourseWithoutSectionsDTOByIdAndPublishedAndDeleted(Long courseId, Boolean published, Boolean deleted);

    @Query("""
        SELECT c.id, c.title, c.thumbnail_url, c.description, c.language, c.subtitles, c.benefits, c.prerequisites, c.price, c.teacher
            FROM course c
            WHERE c.id = :courseId
                AND c.teacher = :teacher
                AND c.published = :published
                AND c.deleted = :deleted
    """)
    Optional<CourseWithoutSectionsDTO> findCourseWithoutSectionsDTOByIdAndPublishedAndDeleted(Long courseId, String teacher, Boolean published, Boolean deleted);

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

    @Query("select co.* from course c join post p on c.id = p.course " +
            "join comment co on co.post = p.id  " +
            "where c.id = :courseId and p.id = :postId and p.deleted = false LIMIT :size OFFSET :page * :size")
    List<Comment> findAllCommentsByCourseIdAndPostId(Long courseId, Long postId, int page, int size);

    @Query("select co.* from course c join post p on c.id = p.course " +
            "join comment co on co.post = p.id  " +
            "where c.id = :courseId and p.id = :postId and c.teacher = :teacher and p.deleted = false LIMIT :size OFFSET :page * :size")
    List<Comment> findAllCommentsByCourseIdAndPostIdAndTeacher(Long courseId, Long postId, String teacher, int page, int size);

    /**
     * Queries for <code>CourseEnrolmentQueryService</code> port
     * */
    @Query("""
    SELECT c.id, c.title, c.thumbnail_url, c.teacher
            FROM course c
            WHERE c.id = :courseId
                AND c.published = :published
    """)
    Optional<CourseInfoDTO> findCourseInfoDTOByIdAndPublishedAndTeacher(long courseId, boolean published);

    @Query("""
    SELECT c.id, c.title, c.thumbnail_url, c.teacher
            FROM course c
            WHERE c.id = :courseId
                AND c.published = :published
                AND c.teacher = :teacher
    """)
    Optional<CourseInfoDTO> findCourseInfoDTOByIdAndPublishedAndTeacher(long courseId, boolean published, String teacher);

    int countCourseByTeacherAndCreatedDateAfterAndPublished(String teacher, LocalDateTime createdDateAfter, Boolean published);

    @Query("""
        SELECT extract(MONTH from c.published_date) as month, COUNT(c.id) AS count
            FROM course c
            WHERE c.teacher = :teacher AND extract(YEAR from c.published_date) = :year AND c.published = true
            GROUP BY extract(MONTH from c.published_date)
    """)
    List<MonthStats> statsMonthPublishedCourseByTeacherAndYear(String teacher, Integer year);

    @Query("""
        SELECT extract(MONTH from c.created_date) as month, COUNT(c.id) AS count
            FROM course c
            WHERE c.teacher = :teacher AND extract(YEAR from c.created_date) = :year AND c.published = false
            GROUP BY extract(MONTH from c.created_date)
    """)
    List<MonthStats> statsMonthDraftCourseByTeacherAndYear(String teacher, Integer year);

}
