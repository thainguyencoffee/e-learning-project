package com.el.enrollment.adapter;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.EnsureEnrolmentCompleted;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.EnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EnsureEnrolmentCompletedImpl implements EnsureEnrolmentCompleted {

    private final EnrollmentRepository enrollmentRepository;

    public EnsureEnrolmentCompletedImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void ensureEnrolmentCompleted(Long enrolmentId, String student) {
        Enrollment enrollment = enrollmentRepository.findByIdAndStudent(enrolmentId, student)
                .orElseThrow(ResourceNotFoundException::new);
        if (!enrollment.getCompleted()) {
            throw new InputInvalidException("Course enrolment is not completed.");
        }
    }

}
