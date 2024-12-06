package com.el.coursepath.application.dto;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.Set;

public record CoursePathPublishedDto(
        Long id,
        String title,
        String description,
        Set<CourseOrderPublishedDto> courseOrders,
        String teacher,
        LocalDateTime publishedDate
) {
    public record CourseOrderPublishedDto(
            Long id,
            Long courseId,
            Integer orderIndex,
            MonetaryAmount price,
            String title,
            Integer purchaseCount
    ) {
        public CourseOrderPublishedDto addPurchaseCount(int purchaseCount) {
            return new CourseOrderPublishedDto(
                    id,
                    courseId,
                    orderIndex,
                    price,
                    title,
                    purchaseCount
            );
        }
    }
}
