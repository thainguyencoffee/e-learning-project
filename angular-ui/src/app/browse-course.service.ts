import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {PageWrapper} from "./administration/courses/model/view/page-wrapper";
import {Course} from "./administration/courses/model/view/course";
import {CourseEnrollmentDTO} from "./my-courses/model";


@Injectable(
  {providedIn: 'root'}
)
export class BrowseCourseService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/published-courses'

  getAllPublishedCourses(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<Course>> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Course>>(url)
  }

  getPublishedCourse(id: number): Observable<Course> {
    return this.http.get<Course>(this.resourcePath + '/' + id);
  }

  countCourseEnrolmentsByCourseId(courseId: number) {
    return this.http.get<number>(`${this.resourcePath}/${courseId}/enrolments/count`);
  }

  getAllCourseEnrolmentsByCourseId(courseId: number): Observable<PageWrapper<CourseEnrollmentDTO>> {
    return this.http.get<PageWrapper<CourseEnrollmentDTO>>(`${this.resourcePath}/${courseId}/enrolments`);
  }

}
