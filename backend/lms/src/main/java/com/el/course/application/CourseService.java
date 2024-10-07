package com.el.course.application;

import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseSectionDTO;
import com.el.course.application.dto.CourseUpdateDTO;
import com.el.course.domain.Course;
import com.el.course.domain.Lesson;

import javax.money.MonetaryAmount;

public interface CourseService {

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

    Course updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson);

    Course removeLesson(Long courseId, Long sectionId, Long lessonId);

    Course updatePrice(Long courseId, MonetaryAmount newPrice);
}
