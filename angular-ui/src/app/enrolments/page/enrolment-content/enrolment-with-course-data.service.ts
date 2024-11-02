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
