import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {CoursePathPublishedDto} from "../model/course-path-published.dto";

@Injectable({
  providedIn: 'root'
})
export class CoursePathPublishedService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/course-paths-published'

  getPublishedCoursePaths(courseId: number, pageNumber: number = 0, pageSize: number = 2) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CoursePathPublishedDto>>(url, { params: { courseId }});
  }

}
