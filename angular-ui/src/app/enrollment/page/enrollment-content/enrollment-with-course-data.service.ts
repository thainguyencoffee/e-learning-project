import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {EnrollmentWithCourseDto} from "../../model/enrollment-with-course-dto";
import {LessonProgress} from "../../model/lesson-progress";

@Injectable({
  providedIn: 'root'
})
export class EnrollmentWithCourseDataService {

  private enrollmentWithCourseSubject = new BehaviorSubject<EnrollmentWithCourseDto | null>(null);
  enrollmentWithCourse$ = this.enrollmentWithCourseSubject.asObservable();

  setEnrollmentWithCourse(enrollmentWithCourse: EnrollmentWithCourseDto): void {
    this.enrollmentWithCourseSubject.next(enrollmentWithCourse);
  }

  clearEnrollmentWithCourse(): void {
    this.enrollmentWithCourseSubject.next(null);
  }

  sortSectionAndLessons(enrollmentWithCourse: EnrollmentWithCourseDto) {
    enrollmentWithCourse.sections.sort((a, b) => a.orderIndex - b.orderIndex);
    enrollmentWithCourse.sections.forEach(section => {
      section.lessons.sort((a, b) => a.orderIndex - b.orderIndex);
    });

    this.setEnrollmentWithCourse(enrollmentWithCourse);

    return enrollmentWithCourse;
  }

  getNextLesson(lessonId: number) {
    const enrollmentWithCourse = this.enrollmentWithCourseSubject.value;
    if (enrollmentWithCourse) {
      for (const section of enrollmentWithCourse.sections) {
        const lessons = section.lessons;
        const lessonIndex = lessons.findIndex(lesson => lesson.id === lessonId);
        if (lessonIndex !== -1) {
          if (lessonIndex + 1 < lessons.length) {
            return lessons[lessonIndex + 1].id;
          } else {
            const nextSectionIndex = enrollmentWithCourse.sections.indexOf(section) + 1;
            if (nextSectionIndex < enrollmentWithCourse.sections.length) {
              return enrollmentWithCourse.sections[nextSectionIndex].lessons[0]?.id || null;
            }
          }
        }
      }
    } else {
      return null;
    }
    return null;
  }

  getPrevLesson(lessonId: number) {
    const enrollmentWithCourse = this.enrollmentWithCourseSubject.value;
    if (enrollmentWithCourse) {
      for (const section of enrollmentWithCourse.sections) {
        const lessons = section.lessons;
        const lessonIndex = lessons.findIndex(lesson => lesson.id === lessonId);

        if (lessonIndex !== -1) {
          if (lessonIndex - 1 >= 0) {
            return lessons[lessonIndex - 1].id;
          } else {
            const prevSectionIndex = enrollmentWithCourse.sections.indexOf(section) - 1;
            if (prevSectionIndex >= 0) {
              const prevSection = enrollmentWithCourse.sections[prevSectionIndex];
              return prevSection.lessons[prevSection.lessons.length - 1]?.id || null;
            }
          }
        }
      }
    } else {
      return null;
    }
    return null;
  }

  getLessonIdPrev(lessonId: number, lessonProgresses: LessonProgress[]) {
    const index = lessonProgresses.findIndex(lp => lp.lessonId === lessonId);
    if (index === 0) {
      return null;
    }
    return lessonProgresses[index - 1]?.lessonId;
  }

  getLessonIdNext(lessonId: number, lessonProgresses: LessonProgress[]) {
    const index = lessonProgresses.findIndex(lp => lp.lessonId === lessonId);
    if (index === lessonProgresses.length - 1) {
      return null;
    }
    return lessonProgresses[index + 1]?.lessonId;
  }

}
