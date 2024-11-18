import {inject, Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {catchError, Observable, switchMap, throwError} from "rxjs";
import {Course} from "../model/view/course";
import {AddCourseDto} from "../model/add-course.dto";
import {UploadService} from "../../../common/upload/upload.service";
import {EditCourseDto} from "../model/edit-course.dto";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {SectionDto} from "../model/section-dto";
import {LessonDto} from "../model/lesson-dto";
import {Section} from "../model/view/section";
import {Lesson} from "../model/view/lesson";
import {CourseRequestDto} from "../model/course-request.dto";
import {ApproveRequestDto} from "../model/approve-request.dto";
import {RejectRequestDto} from "../model/reject-request.dto";
import {Post} from "../model/view/post";
import {PostDto} from "../model/post-dto";
import {EditQuizDto} from "../model/edit-quiz.dto";
import {QuestionDto} from "../model/question.dto";
import {Quiz} from "../model/view/quiz";
import {CommentDto} from "../../../enrolments/model/comment.dto";
import {ReviewDto} from "../model/review.dto";

@Injectable(
  {providedIn: 'root'}
)
export class CourseService {

  http = inject(HttpClient);
  uploadService = inject(UploadService);

  resourcePath = '/bff/api/courses'

  getAllCourses(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<Course>> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Course>>(url)
  }

  getAllCoursesInTrash(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<Course>> {
    const url = `${this.resourcePath}/trash?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Course>>(url)
  }

  getCourse(id: number): Observable<Course> {
    return this.http.get<Course>(this.resourcePath + '/' + id);
  }

  createCourse(data: AddCourseDto): Observable<Course> {
    return this.http.post<Course>(this.resourcePath, data);
  }

  deleteCourseForce(course: Course) {
    const links: string[] = [];

    if (course?.thumbnailUrl) {
      links.push(course.thumbnailUrl);
    }

    course?.sections?.forEach(section => {
      section.lessons?.forEach(lesson => {
        if (lesson.link) {
          links.push(lesson.link);
        }
      });
    });

    if (links) {
      return this.uploadService.deleteAll(links).pipe(
        switchMap(() => this.http.delete<void>(this.resourcePath + '/' + course.id, {
          params: { force: 'true' }
        }))
      );
    } else {
      return this.http.delete<void>(this.resourcePath + '/' + course.id, {
        params: { force: 'true' }
      });
    }

  }

  deleteCourse(id: number) {
    return this.http.delete<void>(this.resourcePath + '/' + id);
  }

  updateCourse(id: number, data: EditCourseDto) {
    return this.http.put<Course>(this.resourcePath + '/' + id, data);
  }

  addSection(id: number, data: SectionDto) {
    return this.http.post(`${this.resourcePath}/${id}/sections`, data)
  }

  deleteSection(currentId: number, sectionId: number, section: Section) {
    const links: string[] = [];
    section.lessons?.forEach(lesson => {
      if (lesson.link) {
        links.push(lesson.link);
      }
    });
    if (links) {
      return this.uploadService.deleteAll(links)
        .pipe(switchMap(() => this.http.delete<void>(`${this.resourcePath}/${currentId}/sections/${sectionId}`)))
    }
    else {
      return this.http.delete<void>(`${this.resourcePath}/${currentId}/sections/${sectionId}`)
    }
  }

  updateSection(courseId: number, sectionId: number | undefined, data: SectionDto) {
    return this.http.put<Course>(`${this.resourcePath}/${courseId}/sections/${sectionId}`, data);
  }

  addLesson(courseId: number, sectionId: number, data: LessonDto) {
    return this.http.post(`${this.resourcePath}/${courseId}/sections/${sectionId}/lessons`, data);
  }

    deleteLesson(courseId: number, sectionId: number, lessonId: number, lesson: Lesson) {
      if (lesson.link) {
        return this.uploadService.deleteAll([lesson.link])
          .pipe(switchMap(() => this.http.delete<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/lessons/${lessonId}`)))
      } else {
        return this.http.delete<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/lessons/${lessonId}`);
      }
    }

  updateLesson(courseId: number, sectionId: number, lessonId: number, data: LessonDto) {
    return this.http.put<Course>(`${this.resourcePath}/${courseId}/sections/${sectionId}/lessons/${lessonId}`, data);
  }

  assignTeacher(courseId: number, teacher: string) {
    return this.http.put(`${this.resourcePath}/${courseId}/assign-teacher`, {teacher: teacher});
  }

  restoreCourse(courseId: number) {
    return this.http.post<void>(`${this.resourcePath}/${courseId}/restore`, {});
  }

  publishCourse(courseId: number) {
    return this.http.put<Course>(`${this.resourcePath}/${courseId}/publish`, {});
  }

  updatePrice(courseId: number, price: string) {
    return this.http.put<Course>(`${this.resourcePath}/${courseId}/update-price`, {price});
  }

  createRequestCourse(courseId: number, data: CourseRequestDto) {
    return this.http.post<void>(`${this.resourcePath}/${courseId}/requests`, data);
  }

  approveRequest(courseId: number, requestId: number, data: ApproveRequestDto) {
    return this.http.put<void>(`${this.resourcePath}/${courseId}/requests/${requestId}/approve`, data);
  }

  rejectRequest(courseId: number, requestId: number, data: RejectRequestDto) {
    return this.http.put<void>(`${this.resourcePath}/${courseId}/requests/${requestId}/reject`, data);
  }

  getAllPosts(pageNumber: number = 0,courseId: number, pageSize: number = 10): Observable<PageWrapper<Post>> {
    const url = `${this.resourcePath}/${courseId}/posts?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Post>>(url);
  }

  getPost(courseId: number, postId: number){
    return this.http.get<Post>(`${this.resourcePath}/${courseId}/posts/${postId}`);
  }

  createPost(data: PostDto, courseId: number): Observable<void> {
    return this.http.post<void>(`${this.resourcePath}/${courseId}/posts`, data);
  }
  updatePost(courseId: number, postId: number, postData: PostDto): Observable<void> {
    return this.http.put<void>(`${this.resourcePath}/${courseId}/posts/${postId}`, postData);
  }

  deletePost(courseId: number, postId: number, post: Post): Observable<void> {
    if (post.attachmentUrls && post.attachmentUrls.length > 0) {
      return this.uploadService.deleteAll(post.attachmentUrls)
        .pipe(switchMap(() => this.http.delete<void>(`${this.resourcePath}/${courseId}/posts/${postId}`)));
    } else {
      return this.http.delete<void>(`${this.resourcePath}/${courseId}/posts/${postId}`);
    }
  }

  restorePost(courseId: number, postId: number){
    return this.http.post<void>(`${this.resourcePath}/${courseId}/posts/${postId}/restore`, {});
  }

  getTrashedPosts(pageNumber: number = 0, courseId: number, pageSize: number = 10): Observable<PageWrapper<Post>> {
    const url = `${this.resourcePath}/${courseId}/posts/trash?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Post>>(url);
  }

  deletePostForce(post: Post, courseId: number): Observable<void> {
    return this.http.delete<void>(`${this.resourcePath}/${courseId}/posts/${post.id}`, {
      params: { force: 'true' }
    });
  }

  addEmotion(courseId: number, postId: number) {
    return this.http.post<number>(`${this.resourcePath}/${courseId}/posts/${postId}/emotions`, {})
  }

  addComment(courseId: number, postId: number, data: CommentDto) {
    return this.http.post<number>(`${this.resourcePath}/${courseId}/posts/${postId}/comments`, data);
  }

  editComment(courseId: number, postId: number, commentId: number, data: CommentDto) {
    return this.http.put<void>(`${this.resourcePath}/${courseId}/posts/${postId}/comments/${commentId}`, data);
  }

  deleteComment(courseId: number, postId: number, commentId: number) {
    return this.http.delete<void>(`${this.resourcePath}/${courseId}/posts/${postId}/comments/${commentId}`)
  }

  getQuiz(courseId: number, sectionId: number) {
    return this.http.get<PageWrapper<Quiz>>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes`);
  }

  addQuizToSection(courseId: number, sectionId: number, data: AddCourseDto) {
    return this.http.post(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes`, data);
  }

  updateQuizInSection(courseId: number, sectionId: number, quizId: number, data: EditQuizDto) {
    return this.http.put(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}`, data);
  }

  addQuestionToQuiz(courseId: number, sectionId: number, quizId: number, data: QuestionDto) {
    return this.http.post(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}/questions`, data);
  }

  updateQuestionToQuiz(courseId: number, sectionId: number, quizId: number, questionId: number, data: QuestionDto) {
    return this.http.put(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}/questions/${questionId}`, data);
  }

  deleteQuestion(courseId: number, sectionId: number, quizId: number, questionId: number) {
    return this.http.delete<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}/questions/${questionId}`);
  }


  deleteQuiz(courseId: number, sectionId: number, quizId: number) {
    return this.http.delete<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}`);
  }

  getQuizInTrash(courseId: number, sectionId: number) {
    return this.http.get<PageWrapper<Quiz>>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/trash`);
  }

  restoreQuiz(courseId: number, sectionId: number, quizId: number) {
    return this.http.post<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}/restore`, {});
  }

  deleteQuizForce(courseId: number, sectionId: number, quizId: number) {
    return this.http.delete<void>(`${this.resourcePath}/${courseId}/sections/${sectionId}/quizzes/${quizId}`, {
      params: { force: 'true' }
    });
  }

  submitReview(courseId: number, enrollmentId: number, data: ReviewDto) {
    return this.http.post<number>(`${this.resourcePath}/${courseId}/reviews?enrollmentId=${enrollmentId}`, data);
  }

}
