import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../common/dto/page-wrapper";
import {EnrolmentDTO} from "../model/enrolment-dto";
import {EnrolmentWithCourseDto} from "../model/enrolment-with-course-dto";
import {Enrolment} from "../model/enrolment";
import {Observable} from "rxjs";
import {CommentDto} from "../model/comment-dto";
import {Emotion} from "../../administration/courses/model/view/emotion";


@Injectable({
  providedIn: 'root'
})
export class EnrolmentsService {

  http = inject(HttpClient);
  baseUrl = '/bff/api/courses';
  resourcePath = '/bff/api/enrollments' // fix typo later

  addComment(courseId: number, postId: number, commentData: CommentDto): Observable<void> {
    const url = `${this.baseUrl}/${courseId}/posts/${postId}/comments`;
    return this.http.post<void>(url, commentData);
  }

  addEmotion(courseId: number, postId: number, username: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${courseId}/posts/${postId}/emotions`, {username});
  }

  deleteEmotion(courseId: number, postId: number, emotionId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${courseId}/posts/${postId}/emotions/${emotionId}`);
  }

  getEmotion(courseId: number, postId: number): Observable<{ id: number } | null> {
    return this.http.get<{ id: number } | null>(`${this.baseUrl}/${courseId}/posts/${postId}/emotions`);
  }

  getAllEnrollments(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<EnrolmentDTO>>(url)
  }

  getEnrolmentWithCourseByEnrollmentId(enrollmentId: number) {
    return this.http.get<EnrolmentWithCourseDto>(`${this.resourcePath}/${enrollmentId}/content`);
  }

  getComments(courseId: number, postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`/courses/${courseId}/posts/${postId}/comments`);
  }

  getEnrollmentById(enrollmentId: string) {
    return this.http.get<EnrolmentDTO>(`${this.resourcePath}/${enrollmentId}`);
  }

  getEnrolmentByCourseId(courseId: number) {
    return this.http.get<Enrolment>(`${this.resourcePath}/course/${courseId}`);
  }

  countEnrolmentsByCourseId(courseId: number) {
    return this.http.get<number>(`${this.resourcePath}/count`, {params: {courseId: courseId.toString()}});
  }

  markLessonAsCompleted(enrollmentId: number, lessonId: number) {
    return this.http.put(`${this.resourcePath}/${enrollmentId}/lessons/${lessonId}?mark=completed`, null);
  }

  markLessonAsIncomplete(enrollmentId: number, lessonId: number) {
    return this.http.put(`${this.resourcePath}/${enrollmentId}/lessons/${lessonId}?mark=incomplete`, null);
  }

}
