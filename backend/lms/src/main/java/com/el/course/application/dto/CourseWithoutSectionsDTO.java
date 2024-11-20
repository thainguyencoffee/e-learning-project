package com.el.course.application.dto;

import com.el.course.domain.Course;
import com.el.course.domain.Language;
import com.el.course.domain.Review;

import javax.money.MonetaryAmount;
import java.util.Set;

public record CourseWithoutSectionsDTO(
        Long id,
        String title,
        String thumbnailUrl,
        String description,
        Language language,
        Set<Language> subtitles,
        Set<String> benefits,
        Set<String> prerequisites,
        MonetaryAmount price,
        String teacher,
        Set<Review> reviews,
        double averageRating
) {

    public static CourseWithoutSectionsDTO fromCourse(Course course) {
        return new CourseWithoutSectionsDTO(
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getDescription(),
                course.getLanguage(),
                course.getSubtitles(),
                course.getBenefits(),
                course.getPrerequisites(),
                course.getPrice(),
                course.getTeacher(),
                course.getReviews(),
                course.getAverageRating()
        );
    }

}
