package com.el.salary.application.impl;

import com.el.course.domain.CourseRepository;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import com.el.salary.application.SalaryService;
import com.el.salary.domain.Salary;
import com.el.salary.domain.SalaryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class SalaryServiceImpl implements SalaryService {

    private final SalaryRepository salaryRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public SalaryServiceImpl(SalaryRepository salaryRepository, CourseRepository courseRepository, CourseEnrollmentRepository courseEnrollmentRepository) {
        this.salaryRepository = salaryRepository;
        this.courseRepository = courseRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @Override
    public void adjustRank(String teacher, boolean byCourse) {
        salaryRepository.findByTeacher(teacher)
                .map(salary -> {
                    if (byCourse) {
                        salary.adjustNumberOfCourses();
                    } else {
                        salary.adjustNumberOfStudents();
                    }
                    return salaryRepository.save(salary);
                }).orElseGet(() -> {
                    Salary salary = new Salary(teacher);
                    if (byCourse) {
                        salary.adjustNumberOfCourses();
                    } else {
                        salary.adjustNumberOfStudents();
                    }
                    return salaryRepository.save(salary);
                });
    }

    @Override
//    @Scheduled(cron = "0 30 13 17 * ?")
    @Scheduled(cron = "0 */10 * * * ?")
    public void addSalaryRecordForAllTeachers() {
        LocalDateTime firstDayOfMonth = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), 1, 0, 0);

        salaryRepository.findAll().forEach(salary -> {
            String teacher = salary.getTeacher();
            int numberOfCourses = courseRepository.countCourseByTeacherAndCreatedDateAfterAndPublished(teacher, firstDayOfMonth, true);
            int numberOfStudents = courseEnrollmentRepository.countCourseEnrollmentByTeacherAndCreatedDateAfter(teacher, firstDayOfMonth);
            salary.addSalaryRecord(numberOfCourses, numberOfStudents);
            salaryRepository.save(salary);
        });
    }

}
