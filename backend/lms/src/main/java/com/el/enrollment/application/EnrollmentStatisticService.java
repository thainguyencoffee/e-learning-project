package com.el.enrollment.application;

import com.el.enrollment.application.dto.CourseInfoWithEnrollmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrollmentsDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentStatisticService {

    List<CourseInfoWithEnrollmentStatisticDTO> getCourseMinInfoWithEnrollmentStatistics(Pageable pageable);

    CourseInfoWithEnrollmentsDTO getCourseWithEnrollmentStatistics(Long courseId);

}
