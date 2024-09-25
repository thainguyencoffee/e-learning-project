package com.elearning.course.application;

import com.elearning.course.domain.Audience;
import com.elearning.course.domain.Course;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;
import java.util.Set;

public record CourseRequestDTO(
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Price is required")
        MonetaryAmount price,
        String description,
        @Valid
        AudienceDTO audience,
        @Valid
        Set<CourseSectionDTO> sections,
        Long discountId,
        @NotBlank(message = "Thumbnail url is required")
        String thumbnailUrl,
        String teacherId
) {

    // Phương thức tĩnh để tạo CourseRequestDTO từ CourseRequestDTO không có teacherId
    public static CourseRequestDTO withTeacherId(CourseRequestDTO dto, String teacherId) {
        return new CourseRequestDTO(
                dto.title(),
                dto.price(),
                dto.description(),
                dto.audience(),
                dto.sections(),
                dto.discountId(),
                dto.thumbnailUrl(),
                teacherId
        );
    }

    public Course toCourse() {
        return new Course(
                title,
                price,
                description,
                new Audience(audience.isPublic(), audience.emailAuthorities()),
                thumbnailUrl,
                teacherId
        );
    }
}
