import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {EnrolmentWithCourseDto} from "../../model/enrolment-with-course-dto";
import {LessonProgress} from "../../model/lesson-progress";

@Injectable({
  providedIn: 'root'
})
export class EnrolmentWithCourseDataService {

  private enrolmentWithCourseSubject = new BehaviorSubject<EnrolmentWithCourseDto | null>(null);
  enrolmentWithCourse$ = this.enrolmentWithCourseSubject.asObservable();

  setEnrolmentWithCourse(enrolmentWithCourse: EnrolmentWithCourseDto): void {
    this.enrolmentWithCourseSubject.next(enrolmentWithCourse);
  }

  clearEnrolmentWithCourse(): void {
    this.enrolmentWithCourseSubject.next(null);
  }

  sortSectionAndLessons(enrolmentWithCourse: EnrolmentWithCourseDto) {
    enrolmentWithCourse.sections.sort((a, b) => a.orderIndex - b.orderIndex);
    enrolmentWithCourse.sections.forEach(section => {
      section.lessons.sort((a, b) => a.orderIndex - b.orderIndex);
    });

    this.setEnrolmentWithCourse(enrolmentWithCourse);

    return enrolmentWithCourse;
  }

  getNextLesson(lessonId: number) {
    const enrolmentWithCourse = this.enrolmentWithCourseSubject.value;
    if (enrolmentWithCourse) {
      for (const section of enrolmentWithCourse.sections) {
        const lessons = section.lessons;
        const lessonIndex = lessons.findIndex(lesson => lesson.id === lessonId);
        if (lessonIndex !== -1) {
          if (lessonIndex + 1 < lessons.length) {
            return lessons[lessonIndex + 1].id;
          } else {
            const nextSectionIndex = enrolmentWithCourse.sections.indexOf(section) + 1;
            if (nextSectionIndex < enrolmentWithCourse.sections.length) {
              return enrolmentWithCourse.sections[nextSectionIndex].lessons[0]?.id || null;
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
    const enrolmentWithCourse = this.enrolmentWithCourseSubject.value;
    if (enrolmentWithCourse) {
      for (const section of enrolmentWithCourse.sections) {
        const lessons = section.lessons;
        const lessonIndex = lessons.findIndex(lesson => lesson.id === lessonId);

        if (lessonIndex !== -1) {
          if (lessonIndex - 1 >= 0) {
            return lessons[lessonIndex - 1].id;
          } else {
            const prevSectionIndex = enrolmentWithCourse.sections.indexOf(section) - 1;
            if (prevSectionIndex >= 0) {
              const prevSection = enrolmentWithCourse.sections[prevSectionIndex];
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
