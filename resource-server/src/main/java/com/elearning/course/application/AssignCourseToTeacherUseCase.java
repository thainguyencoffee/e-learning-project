package com.elearning.course.application;

import com.elearning.common.UseCase;

@UseCase
public class AssignCourseToTeacherUseCase {

    private final CourseQueryUseCase courseQueryUseCase;

    public AssignCourseToTeacherUseCase(CourseQueryUseCase courseQueryUseCase) {
        this.courseQueryUseCase = courseQueryUseCase;
    }

    public void execute(Long courseId, String teacher) {
        courseQueryUseCase.findCourseById(courseId).assignTeacher(teacher);
    }

}
