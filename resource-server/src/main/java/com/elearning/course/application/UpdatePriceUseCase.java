package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;

@UseCase
public class UpdatePriceUseCase {

    private final CourseRepository courseRepository;

    public UpdatePriceUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course execute(Long courseId, MonetaryAmount newPrice) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);

        // Sử dụng phương thức changePrice để cập nhật giá
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }

}
