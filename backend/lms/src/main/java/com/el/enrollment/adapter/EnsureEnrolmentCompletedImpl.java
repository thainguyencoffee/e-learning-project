package com.el.enrollment.adapter;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.EnsureEnrolmentCompleted;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EnsureEnrolmentCompletedImpl implements EnsureEnrolmentCompleted {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public EnsureEnrolmentCompletedImpl(CourseEnrollmentRepository courseEnrollmentRepository) {
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @Override
    public void ensureEnrolmentCompleted(Long enrolmentId, String student) {
        CourseEnrollment courseEnrollment = courseEnrollmentRepository.findByIdAndStudent(enrolmentId, student)
                .orElseThrow(ResourceNotFoundException::new);
        if (!courseEnrollment.getCompleted()) {
            throw new InputInvalidException("Course enrolment is not completed.");
        }
    }

}
