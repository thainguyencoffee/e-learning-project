package com.el.course.application.dto.teacher;

import com.el.common.projection.MonthStats;
import com.el.common.projection.RatingMonthStats;

import java.util.List;

public record TeacherStatisticsDTO(
    List<MonthStats> coursesByMonth,
    List<MonthStats> draftCoursesByMonth,
    List<MonthStats> studentsEnrolledByMonth,
    List<StudentsByCourseDTO> studentsByCourse,
    List<RatingMonthStats> ratingOverallByMonth) {
}