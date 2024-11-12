import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {CourseInfoWithEnrolmentStatisticDto} from "../model/course-info-with-enrolment-statistic.dto";
import {Observable} from "rxjs";
import {CourseInfoWithEnrolmentsDto} from "../model/course-info-with-enrolments.dto";

@Injectable({
  providedIn: 'root'
})
export class EnrolmentStatisticService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/enrollments/statistics';

  getCourseInfoWithEnrolmentStatisticDTO(pageNumber: number = 0, pageSize: number = 10):
      Observable<PageWrapper<CourseInfoWithEnrolmentStatisticDto>> {

    return this.http.get<PageWrapper<CourseInfoWithEnrolmentStatisticDto>>(`${this.resourcePath}?page=${pageNumber}&size=${pageSize}`);
  }

  getCourseInfoWithEnrolments(courseId: number): Observable<CourseInfoWithEnrolmentsDto> {
    return this.http.get<CourseInfoWithEnrolmentsDto>(`${this.resourcePath}/${courseId}`);
  }

}
