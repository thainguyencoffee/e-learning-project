package com.el.enrollment.adapter;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.EnsureEnrollmentCompleted;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.EnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EnsureEnrollmentCompletedImpl implements EnsureEnrollmentCompleted {

    private final EnrollmentRepository enrollmentRepository;

    public EnsureEnrollmentCompletedImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void ensureEnrollmentCompleted(Long enrollmentId, String student) {
        Enrollment enrollment = enrollmentRepository.findByIdAndStudent(enrollmentId, student)
                .orElseThrow(ResourceNotFoundException::new);
        if (!enrollment.getCompleted()) {
            throw new InputInvalidException("Course enrollment is not completed.");
        }
    }

}
