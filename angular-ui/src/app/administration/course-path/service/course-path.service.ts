import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CoursePathDto} from "../model/course-path.dto";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {CoursePath} from "../model/course-path";
import {CoursePathInTrashDto} from "../model/course-path-in-trash.dto";

@Injectable({
  providedIn: 'root'
})
export class CoursePathService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/course-paths'

  createCoursePath(data: CoursePathDto) {
    return this.http.post(this.resourcePath, data);
  }

  getCoursePaths(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CoursePath>>(url)
  }

  getCoursePath(coursePathId: number) {
    return this.http.get<CoursePath>(`${this.resourcePath}/${coursePathId}`);
  }

  getTrashedCoursePaths(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}/trash?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CoursePathInTrashDto>>(url);
  }

  deleteCoursePath(coursePathId: number) {
    return this.http.delete(`${this.resourcePath}/${coursePathId}`);
  }

  publish(coursePathId: number) {
    return this.http.post(`${this.resourcePath}/${coursePathId}/publish`, null);
  }

  unpublish(coursePathId: number) {
    return this.http.post(`${this.resourcePath}/${coursePathId}/unpublish`, null);
  }

  addCourseOrder(coursePathId: number, courseId: number) {
    return this.http.post(`${this.resourcePath}/${coursePathId}/courseOrders`, { courseId });
  }

  removeCourseOrder(coursePathId: number, courseId: number) {
    return this.http.delete(`${this.resourcePath}/${coursePathId}/courseOrders/${courseId}`);
  }

  restoreCoursePath(coursePathId: number) {
    return this.http.post(`${this.resourcePath}/${coursePathId}/restore`, null);
  }

  deleteForce(coursePathId: number) {
    return this.http.delete(`${this.resourcePath}/${coursePathId}`, {
      params: { force: 'true' }
    });
  }

  updateCoursePath(coursePathId: number, data: CoursePathDto) {
    return this.http.put(`${this.resourcePath}/${coursePathId}`, data);
  }

}
