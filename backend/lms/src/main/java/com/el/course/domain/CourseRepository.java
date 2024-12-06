package com.el.course.domain;

import com.el.common.projection.MonthStats;
import com.el.common.projection.RatingMonthStats;
import com.el.course.application.dto.CourseInTrashDTO;
import com.el.course.application.dto.PostInTrashDTO;
import com.el.course.application.dto.QuizInTrashDTO;
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

    Page<Course> findAll(Pageable pageable);

    Page<Course> findAllByDeleted(Boolean deleted, Pageable pageable);

    Page<Course> findAllByTeacherAndDeleted(String teacher, Boolean deleted, Pageable pageable);

    Page<Course> findAllByTeacherAndPublished(String teacher, boolean published, Pageable pageable);

    Optional<Course> findByIdAndDeleted(Long courseId, Boolean deleted);

    Optional<Course> findByIdAndDeletedAndTeacher(Long id, Boolean deleted, String teacher);

    Optional<Course> findByIdAndDeletedAndPublished(Long id, Boolean deleted, Boolean published);

    Page<Course> findAllByDeletedAndPublished(Boolean deleted, Boolean published, Pageable pageable);

    @Query("""
        SELECT 
            c.id, 
            c.title, 
            c.thumbnail_url, 
            c.description, 
            c.teacher, 
            c.language
        FROM 
            course c
        WHERE c.deleted = true
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseInTrashDTO> findAllCoursesInTrash(int page, int size);

    @Query("""
        SELECT 
            c.id, 
            c.title, 
            c.thumbnail_url, 
            c.description, 
            c.teacher, 
            c.language
        FROM 
            course c
        WHERE c.teacher = :teacher 
          AND c.deleted = true
        LIMIT :size OFFSET :page * :size
    """)
    List<CourseInTrashDTO> findAllCoursesInTrashByTeacher(String teacher, int page, int size);

    /*Posts*/
    @Query("""
        SELECT 
            p.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and p.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Post> findAllPostsByCourseId(Long courseId, int page, int size);

    @Query("""
        SELECT 
            p.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and c.teacher = :teacher 
          and p.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Post> findAllPostsByCourseIdAndTeacher(Long courseId, String teacher, int page, int size);

    @Query("""
        SELECT 
            p.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and p.id = :postId 
          and p.deleted = false
    """)
    Optional<Post> findPostByCourseIdAndPostId(Long courseId, Long postId);

    @Query("""
        SELECT 
            p.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and p.id = :postId 
          and c.teacher = :teacher 
          and p.deleted = false
    """)
    Optional<Post> findPostByCourseIdAndPostIdAndTeacher(Long courseId, Long postId, String teacher);

    @Query("""
        SELECT 
            co.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        JOIN comment co on co.post = p.id  
        WHERE c.id = :courseId 
          and p.id = :postId 
          and p.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Comment> findAllCommentsByCourseIdAndPostId(Long courseId, Long postId, int page, int size);

    @Query("""
        SELECT 
            co.* 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        JOIN comment co on co.post = p.id  
        WHERE c.id = :courseId 
          and p.id = :postId 
          and c.teacher = :teacher 
          and p.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Comment> findAllCommentsByCourseIdAndPostIdAndTeacher(Long courseId, Long postId, String teacher, int page, int size);

    @Query("""
        SELECT 
            p.id, 
            p.content, 
            p.username, 
            p.created_date 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and p.deleted = true
        LIMIT :size OFFSET :page * :size
    """)
    List<PostInTrashDTO> findAllPostsInTrashByCourseId(Long courseId, int page, int size);

    @Query("""
        SELECT 
            p.id, 
            p.content, 
            p.username, 
            p.created_date 
        FROM 
            course c 
        JOIN post p on c.id = p.course 
        WHERE c.id = :courseId 
          and p.deleted = true 
          and c.teacher = :teacher
        LIMIT :size OFFSET :page * :size
    """)
    List<PostInTrashDTO> findAllPostsInTrashByCourseIdAndTeacher(Long courseId, String teacher, int page, int size);

    /*Quizzes*/
    @Query("""
        SELECT 
            q.* 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and c.deleted = false 
          and q.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Quiz> findAllQuizzesByCourseIdAndSectionId(Long courseId, Long sectionId, int page, int size);

    @Query("""
        SELECT 
            q.* 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and c.teacher = :teacher 
          and c.deleted = false 
          and q.deleted = false 
        LIMIT :size OFFSET :page * :size
    """)
    List<Quiz> findAllQuizzesByCourseIdAndSectionIdAndTeacher(Long courseId, Long sectionId, String teacher, int page, int size);

    @Query("""
        SELECT 
            q.* 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and q.id = :quizId
          and c.deleted = false 
          and q.deleted = false
    """)
    Optional<Quiz> findQuizByCourseIdAndSectionIdAndQuizId(Long courseId, Long sectionId, Long quizId);


    @Query("""
        SELECT 
            q.* 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and q.id = :quizId 
          and c.teacher = :teacher 
          and c.deleted = false 
          and q.deleted = false
    """)
    Optional<Quiz> findQuizByCourseIdAndSectionIdAndQuizIdAndTeacher(Long courseId, Long sectionId, Long quizId, String teacher);

    @Query("""
        SELECT 
            q.id, 
            q.title, 
            q.description, 
            q.after_lesson_id 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and c.deleted = false 
          and q.deleted = true 
        LIMIT :size OFFSET :page * :size
    """)
    List<QuizInTrashDTO> findAllPostsInTrashByCourseIdAndSectionId(Long courseId, Long sectionId, int page, int size);

    @Query(""" 
        SELECT 
            q.id, 
            q.title, 
            q.description, 
            q.after_lesson_id 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course 
        JOIN quiz q on s.id = q.course_section 
        WHERE c.id = :courseId 
          and s.id = :sectionId 
          and c.teacher = :teacher 
          and c.deleted = false 
          and q.deleted = true 
        LIMIT :size OFFSET :page * :size
    """)
    List<QuizInTrashDTO> findAllPostsInTrashByCourseIdAndSectionIdAndTeacher(Long courseId, Long sectionId, String teacher, int page, int size);

    @Query("""
        SELECT 
            q.* 
        FROM 
            course c 
        JOIN course_section s on c.id = s.course
        JOIN quiz q on s.id = q.course_section
        WHERE q.id = :quizId 
          and c.deleted = false 
          and q.deleted = false
    """)
    Optional<Quiz> findQuizByQuizId(Long quizId);

    /*Other, Statistics*/
    int countCourseByTeacherAndCreatedDateAfterAndPublished(String teacher, LocalDateTime createdDateAfter, Boolean published);

    @Query("""
        SELECT 
            extract(MONTH from c.published_date) as month, COUNT(c.id) AS count
        FROM 
            course c
        WHERE c.teacher = :teacher AND extract(YEAR from c.published_date) = :year AND c.published = true
        GROUP BY extract(MONTH from c.published_date)
    """)
    List<MonthStats> statsMonthPublishedCourseByTeacherAndYear(String teacher, Integer year);

    @Query("""
        SELECT 
            extract(MONTH from c.created_date) as month, COUNT(c.id) AS count
        FROM 
            course c
        WHERE c.teacher = :teacher AND extract(YEAR from c.created_date) = :year AND c.published = false
        GROUP BY extract(MONTH from c.created_date)
    """)
    List<MonthStats> statsMonthDraftCourseByTeacherAndYear(String teacher, Integer year);

    @Query("""
        SELECT 
            EXTRACT(MONTH from r.review_date) as month, AVG(r.rating) as rating
        FROM 
            review r
        JOIN 
            course c ON r.course = c.id
        WHERE 
            c.teacher = :teacher AND extract(YEAR from r.review_date) = :year
        GROUP BY EXTRACT(MONTH from r.review_date)
    """)
    List<RatingMonthStats> statsMonthRatingOverallByTeacherAndYear(String teacher, Integer year);

    /*Combination*/
    @Query("""
        SELECT 
            c.id, 
            c.title, 
            c.thumbnail_url, 
            c.teacher
        FROM 
            course c
        WHERE c.id = :courseId 
          AND c.published = :published
    """)
    Optional<CourseInfoDTO> findCourseInfoDTOByIdAndPublishedAndTeacher(long courseId, boolean published);

    @Query("""
        SELECT 
            c.id, 
            c.title, 
            c.thumbnail_url, 
            c.teacher
        FROM 
            course c
        WHERE c.id = :courseId 
          AND c.published = :published 
          AND c.teacher = :teacher
    """)
    Optional<CourseInfoDTO> findCourseInfoDTOByIdAndPublishedAndTeacher(long courseId, boolean published, String teacher);


    /*Teacher Service*/
    @Query("""
        SELECT 
            c.teacher,
            COUNT(DISTINCT CASE WHEN c.published = TRUE THEN c.id END) as number_of_courses,
            COUNT(e.id) as number_of_students,
            COUNT(CASE when e.completed = TRUE THEN 1 END) as number_of_certificates,
            COUNT(DISTINCT CASE WHEN c.published = FALSE THEN c.id END) as number_of_draft_courses
        FROM 
            course c
        LEFT JOIN 
            enrollment e ON c.id = e.course_id
        WHERE c.teacher = :teacher
        GROUP BY c.teacher
    """)
    CountDataDTO getCountDataDTOByTeacher(String teacher);

    @Query("""
        SELECT 
            c.*
        FROM 
            course c
        WHERE 
            to_tsvector('english', 
                c.title || ' ' || 
                c.teacher || ' ' || 
                c.description) @@ plainto_tsquery(:query)
    """)
    List<Course> searchPublishedCourses(String query, int page, int size);
}
