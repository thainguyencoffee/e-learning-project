import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {CourseMinInfoWithEnrolmentStatisticDTO} from "../model/course-min-info-with-enrolment-statistic.dto";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class EnrolmentStatisticService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/enrollments/statistics';

  getCourseMinInfoWithEnrolmentStatisticDTO(pageNumber: number = 0, pageSize: number = 10):
      Observable<PageWrapper<CourseMinInfoWithEnrolmentStatisticDTO>> {

    return this.http.get<PageWrapper<CourseMinInfoWithEnrolmentStatisticDTO>>(`${this.resourcePath}?page=${pageNumber}&size=${pageSize}`);
  }

}
