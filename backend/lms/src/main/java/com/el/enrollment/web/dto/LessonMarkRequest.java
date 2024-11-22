package com.el.enrollment.web.dto;

public record LessonMarkRequest(
        MarkType mark,
        Long courseId,
        Long lessonId
) {

    public enum MarkType {
        COMPLETED,
        INCOMPLETE
    }

}
