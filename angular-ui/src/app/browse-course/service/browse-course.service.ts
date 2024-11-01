import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {PageWrapper} from "../../common/dto/page-wrapper";
import {CourseWithoutSections} from "../model/course-without-sections";


@Injectable(
  {providedIn: 'root'}
)
export class BrowseCourseService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/published-courses'

  getAllPublishedCourses(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<CourseWithoutSections>> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CourseWithoutSections>>(url)
  }

  getPublishedCourse(courseId: number): Observable<CourseWithoutSections> {
    return this.http.get<CourseWithoutSections>(this.resourcePath + '/' + courseId);
  }

}
