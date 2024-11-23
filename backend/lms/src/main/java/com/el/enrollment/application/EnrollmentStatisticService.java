package com.el.enrollment.application;

import com.el.enrollment.application.dto.CourseInfoWithEnrolmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentsDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentStatisticService {

    List<CourseInfoWithEnrolmentStatisticDTO> getCourseMinInfoWithEnrolmentStatistics(Pageable pageable);

    CourseInfoWithEnrolmentsDTO getCourseWithEnrolmentStatistics(Long courseId);

}
