import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../administration/courses/model/view/page-wrapper";
import {CourseEnrollmentDTO} from "./model";


@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/enrollments'

  getAllEnrollments(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CourseEnrollmentDTO>>(url)
  }

  getEnrollment(enrollmentId: string) {
    return this.http.get<CourseEnrollmentDTO>(`${this.resourcePath}/${enrollmentId}`);
  }

  markLessonAsCompleted(enrollmentId: string, lessonId: string) {
    return this.http.put(`${this.resourcePath}/${enrollmentId}/lessons/${lessonId}mark?completed`, null);
  }

  markLessonAsIncomplete(enrollmentId: string, lessonId: string) {
    return this.http.put(`${this.resourcePath}/${enrollmentId}/lessons/${lessonId}mark?incomplete`, null);
  }

}
