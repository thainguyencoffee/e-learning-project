package com.el.coursepath.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "course_order")
@Getter
public class CourseOrder {
    @Id
    private Long id;
    private Long courseId;
    private Integer orderIndex;

    public CourseOrder(Long courseId, Integer orderIndex) {
        if (courseId == null)
            throw new InputInvalidException("Course ID is required. Please provide a valid course ID.");
        if (orderIndex == null)
            throw new InputInvalidException("Order is required. Please provide a valid order.");
        this.courseId = courseId;
        this.orderIndex = orderIndex;
    }

}
