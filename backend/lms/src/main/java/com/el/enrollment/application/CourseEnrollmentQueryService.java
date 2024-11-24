package com.el.enrollment.application;

import com.el.enrollment.application.dto.CourseInfoDTO;

public interface CourseEnrollmentQueryService {

    CourseInfoDTO findCourseWithoutSectionsDTOByIdAndPublished(long courseId, boolean published);

}
