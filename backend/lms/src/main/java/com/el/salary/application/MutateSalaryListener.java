package com.el.salary.application;

import com.el.course.domain.Course;
import com.el.enrollment.domain.Enrollment;
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
    public void onEnrol(Enrollment.EnrollmentCreatedEvent event) {
        salaryService.adjustRank(event.teacher(), false);
    }

    @ApplicationModuleListener
    public void onEnrollmentRemoved(Enrollment.EnrollNewTeacherCourseEvent event) {
        salaryService.studentChanged(event.oldTeacher(), event.newTeacher());
    }

}
