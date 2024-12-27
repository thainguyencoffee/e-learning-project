package com.el.salary.application.impl;

import com.el.common.exception.ResourceNotFoundException;
import com.el.course.domain.CourseRepository;
import com.el.enrollment.domain.EnrollmentRepository;
import com.el.salary.application.SalaryService;
import com.el.salary.domain.Salary;
import com.el.salary.domain.SalaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Slf4j
@Service
public class SalaryServiceImpl implements SalaryService {

    private final SalaryRepository salaryRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public SalaryServiceImpl(SalaryRepository salaryRepository, CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.salaryRepository = salaryRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void adjustRank(String teacher, boolean byCourse) {
        salaryRepository.findByTeacher(teacher)
                .map(salary -> {
                    if (byCourse) {
                        log.info("Adjusting rank for teacher {} by course, detail: {}", teacher, salary);
                        salary.adjustNumberOfCourses();
                    } else {
                        log.info("Adjusting rank for teacher {} by student, detail: {}", teacher, salary);
                        salary.adjustNumberOfStudents();
                    }
                    return salaryRepository.save(salary);
                }).orElseGet(() -> {
                    log.info("Creating new salary record for teacher {}", teacher);
                    Salary salary = new Salary(teacher);
                    if (byCourse) {
                        log.info("Adjusting rank for teacher after created course {} by course, detail: {}", teacher, salary);
                        salary.adjustNumberOfCourses();
                    } else {
                        log.info("Adjusting rank for teacher after created course {} by student, detail: {}", teacher, salary);
                        salary.adjustNumberOfStudents();
                    }
                    return salaryRepository.save(salary);
                });
    }

    @Override
    public void decreaseStudent(String teacher) {
        salaryRepository.findByTeacher(teacher)
                .map(salary -> {
                    log.info("Decreasing number of students for teacher {}, now is: {}", teacher, salary.getNosAllTime());
                    salary.decreaseNumberOfStudents();
                    return salaryRepository.save(salary);
                }).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    @Transactional
    public void studentChanged(String oldTeacher, String newTeacher) {
        decreaseStudent(oldTeacher);
        adjustRank(newTeacher, false);
    }

    @Override
//    @Scheduled(cron = "0 30 13 17 * ?")
    @Scheduled(cron = "0 */30 * * * ?")
    public void addSalaryRecordForAllTeachers() {
        LocalDateTime firstDayOfMonth = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), 1, 0, 0);

        salaryRepository.findAll().forEach(salary -> {
            String teacher = salary.getTeacher();
            int numberOfCourses = courseRepository.countCourseByTeacherAndCreatedDateAfterAndPublished(teacher, firstDayOfMonth, true);
            int numberOfStudents = enrollmentRepository.countCourseEnrollmentByTeacherAndCreatedDateAfter(teacher, firstDayOfMonth);
            salary.addSalaryRecord(numberOfCourses, numberOfStudents);
            salaryRepository.save(salary);
        });
    }

}
