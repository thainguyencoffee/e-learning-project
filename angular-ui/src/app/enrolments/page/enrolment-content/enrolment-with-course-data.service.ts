import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {EnrolmentWithCourseDto} from "../../model/enrolment-with-course-dto";

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

}
