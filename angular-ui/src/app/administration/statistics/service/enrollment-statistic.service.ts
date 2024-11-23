import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {CourseInfoWithEnrollmentStatisticDto} from "../model/course-info-with-enrollment-statistic.dto";
import {Observable} from "rxjs";
import {CourseInfoWithEnrollmentsDto} from "../model/course-info-with-enrollments.dto";

@Injectable({
  providedIn: 'root'
})
export class EnrollmentStatisticService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/enrollments/statistics';

  getCourseInfoWithEnrollmentStatisticDTO(pageNumber: number = 0, pageSize: number = 10):
      Observable<PageWrapper<CourseInfoWithEnrollmentStatisticDto>> {

    return this.http.get<PageWrapper<CourseInfoWithEnrollmentStatisticDto>>(`${this.resourcePath}?page=${pageNumber}&size=${pageSize}`);
  }

  getCourseInfoWithEnrollments(courseId: number): Observable<CourseInfoWithEnrollmentsDto> {
    return this.http.get<CourseInfoWithEnrollmentsDto>(`${this.resourcePath}/${courseId}`);
  }

}
