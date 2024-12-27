import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../common/dto/page-wrapper";
import {EnrollmentDto} from "../model/enrollment-dto";
import {EnrollmentWithCourseDto} from "../model/enrollment-with-course-dto";
import {Enrollment} from "../model/enrollment";
import {QuizDetailDto} from "../model/quiz-detail.dto";
import {QuizSubmitDto} from "../model/quiz-submit.dto";
import {QuizSubmission} from "../model/quiz-submission";
import {LessonMarkRequest, MarkType} from "../model/lesson-mark-request";
import {ChangeCourseResponse} from "../../browse-course/model/change-course-response";

@Injectable({
  providedIn: 'root'
})
export class EnrollmentsService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/enrollments'

  getAllEnrollments(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<EnrollmentDto>>(url)
  }

  getEnrollmentWithCourseByEnrollmentId(enrollmentId: number) {
    return this.http.get<EnrollmentWithCourseDto>(`${this.resourcePath}/${enrollmentId}/content`);
  }

  getEnrollmentByCourseId(courseId: number) {
    return this.http.get<Enrollment>(`${this.resourcePath}/course/${courseId}`);
  }

  countEnrollmentsByCourseId(courseId: number) {
    return this.http.get<number>(`${this.resourcePath}/count`, {params: {courseId: courseId.toString()}});
  }

  markLessonAsCompleted(enrollmentId: number, courseId: number, lessonId: number) {
    const lessonMarkRequest: LessonMarkRequest = {
      mark: MarkType.COMPLETED,
      courseId,
      lessonId
    }
    return this.http.put(`${this.resourcePath}/${enrollmentId}/mark-lesson`, lessonMarkRequest);
  }

  markLessonAsIncomplete(enrollmentId: number, courseId: number, lessonId: number) {
    const lessonMarkRequest: LessonMarkRequest = {
      mark: MarkType.INCOMPLETE,
      courseId,
      lessonId
    }
    return this.http.put(`${this.resourcePath}/${enrollmentId}/mark-lesson`, lessonMarkRequest);
  }

  getQuiz(enrollmentId: number, quizId: number) {
    return this.http.get<QuizDetailDto>(`${this.resourcePath}/${enrollmentId}/quizzes/${quizId}`);
  }

  isSubmittedQuiz(enrollmentId: number, quizId: number) {
    return this.http.get<boolean>(`${this.resourcePath}/${enrollmentId}/is-submitted-quiz`, {
      params: { quizId }
    });
  }

  submitQuiz(enrollmentId: number, data: QuizSubmitDto) {
    return this.http.post<number>(`${this.resourcePath}/${enrollmentId}/submit-quiz`, data);
  }

  getQuizSubmission(enrollmentId: number, quizSubmissionId: number) {
    return this.http.get<QuizSubmission>(`${this.resourcePath}/${enrollmentId}/quizzes/${quizSubmissionId}/submission`);
  }

  deleteSubmission(enrollmentId: number, quizSubmissionId: number) {
    return this.http.delete(`${this.resourcePath}/${enrollmentId}/quizzes/${quizSubmissionId}/submission`);
  }

  changeCourse(enrollmentId: number, courseId: number) {
    return this.http.put<ChangeCourseResponse>(`${this.resourcePath}/${enrollmentId}/change-course`, {}, {
      params: { courseId }
    });
  }

  getPurchasedCourses() {
    return this.http.get<number[]>(`${this.resourcePath}/purchased-courses`);
  }

}
