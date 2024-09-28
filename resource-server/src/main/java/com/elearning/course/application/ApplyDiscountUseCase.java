package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.discount.application.CalculateDiscountUseCase;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;

@UseCase
public class ApplyDiscountUseCase {

    private final CourseRepository courseRepository;
    private final CalculateDiscountUseCase calculateDiscountUseCase;

    public ApplyDiscountUseCase(CourseRepository courseRepository, CalculateDiscountUseCase calculateDiscountUseCase) {
        this.courseRepository = courseRepository;
        this.calculateDiscountUseCase = calculateDiscountUseCase;
    }

    @Transactional
    public Course execute(Long courseId, Long discountId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);
        MonetaryAmount discountedPrice = calculateDiscountUseCase.execute(discountId, course.getPrice());
        course.applyDiscount(discountedPrice, discountId);

        return courseRepository.save(course);
    }


}
