package com.elearning.course.application;

import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.money.MonetaryAmount;

public interface CourseService {

    Page<Course> findAllCourses(Pageable pageable);

    Course findCourseById(Long courseId);

    Course findCourseDeleted(Long courseId);

    Course createCourse(String teacherId, CourseDTO courseDTO);

    Course updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO);

    void deleteCourse(Long courseId);

    void restoreCourse(Long courseId);

    Course publishCourse(Long courseId, String approvedBy);

    Course assignTeacher(Long courseId, String teacher);

    Course applyDiscount(Long courseId, String discountCode);

    Course addSection(Long courseId, CourseSectionDTO courseSectionDTO);

    Course updateSectionInfo(Long courseId, Long sectionId, String newTitle);

    Course removeSection(Long courseId, Long sectionId);

    Course addLesson(Long courseId, Long sectionId, Lesson lesson);

    Course updatePrice(Long courseId, MonetaryAmount newPrice);
}
