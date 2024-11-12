package com.el.enrollment.application;

import com.el.enrollment.application.dto.CourseInfoWithEnrolmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentsDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrolmentStatisticService {

    List<CourseInfoWithEnrolmentStatisticDTO> getCourseMinInfoWithEnrolmentStatistics(Pageable pageable);

    CourseInfoWithEnrolmentsDTO getCourseWithEnrolmentStatistics(Long courseId);

}
