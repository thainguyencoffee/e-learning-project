package com.el.salary.application;

import com.el.course.domain.Course;
import com.el.enrollment.domain.CourseEnrollment;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class MutateSalaryListener {

    private final SalaryService salaryService;

    public MutateSalaryListener(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @ApplicationModuleListener
    public void onCoursePublished(Course.CoursePublishedEvent event) {
        salaryService.adjustRank(event.teacher(), true);
    }

    @ApplicationModuleListener
    public void onEnrol(CourseEnrollment.EnrolmentCreatedEvent event) {
        salaryService.adjustRank(event.teacher(), false);
    }

}
