import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Observable, Subscription} from "rxjs";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {AsyncPipe, NgIf} from "@angular/common";
import {Section} from "../../../../administration/courses/model/view/section";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {VideoPlayerComponent} from "../../../../common/video-player/video-player.component";
import {DocumentViewerComponent} from "../../../../common/document-viewer/document-viewer.component";
import {LessonProgress} from "../../../model/lesson-progress";
import {QuizSubmission} from "../../../model/quiz-submission";

@Component({
  selector: 'app-lesson-detail',
  standalone: true,
  imports: [
    AsyncPipe,
    NgIf,
    VideoPlayerComponent,
    DocumentViewerComponent,
    RouterLink
  ],
  templateUrl: './lesson-detail.component.html',
})
export class LessonDetailComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  lessonId?: number;
  lessonPrev?: number;
  lessonNext?: number;
  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;
  navigationSubscription?: Subscription;
  lessonType: 'video' | 'docx' = 'video';

  ngOnInit(): void {
    this.loadData();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    });
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
  }

  loadData(): void {
    this.enrolmentId = +this.route.snapshot.params['enrolmentId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.route.queryParams.subscribe(params => {
      this.lessonPrev = +params['lessonPrev'];
      this.lessonNext = +params['lessonNext'];
    });

    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => {
          const sortedData = this.enrolmentWithCourseDataService.sortSectionAndLessons(data);
          this.enrolmentWithCourseDataService.setEnrolmentWithCourse(sortedData);

          // Lấy link bài học từ dữ liệu trả về và xác định loại tài liệu
          const lesson = this.getLessonById(this.lessonId!, sortedData.sections);
          if (lesson && lesson.link) {
            this.setLessonType(lesson.link);  // Gọi setLessonType ở đây
          }
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  getLessonById(lessonId: number, sections: Section[]) {
    return sections.flatMap(s => s.lessons).find(l => l.id === lessonId);
  }

  lessonIsCompleted(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.some(lp => lp.lessonId === lessonId && lp.completed);
  }

  isLessonRequire(lessonId: number, lessonProgresses: LessonProgress[]) {
    const lessonProgress = lessonProgresses.find(lp => lp.lessonId === lessonId);
    if (lessonProgress) {
      return !lessonProgress.bonus;
    }
    return false;
  }

  markLessonAsCompleted(courseId: number, lessonId: number, sections: Section[], quizSubmissions: QuizSubmission[]) {
    const quiz = this.getQuizByLessonId(lessonId, sections);
    this.completeLesson(courseId, lessonId);

    if (quiz) {
      const quizSubmission = this.getQuizSubmissionByLessonId(lessonId, sections, quizSubmissions);
      if (!quizSubmission) {
        this.router.navigate(['/enrolments', this.enrolmentId, 'quiz-submit', quiz.id]);
      }
    }
  }

  private completeLesson(courseId: number, lessonId: number) {
    this.enrolmentService.markLessonAsCompleted(this.enrolmentId!, courseId, lessonId).subscribe({
      next: _ => this.loadData(),
      error: error => this.errorHandler.handleServerError(error.error),
    });
  }

  markLessonAsIncomplete(courseId: number, lessonId: number, title: string): void {
    this.enrolmentService.markLessonAsIncomplete(this.enrolmentId!, courseId, lessonId)
      .subscribe({
        next: _ => {
          const queryParams = this.route.snapshot.queryParams;
          this.router.navigate(['.'], {
            relativeTo: this.route,
            queryParams: queryParams,
            state: { msgSuccess: this.getMessage('unmark', { title }) }
          });
        },
        error: error => this.errorHandler.handleServerError(error.error)
      });
  }

  getQuizByLessonId(lessonId: number, sections: Section[]) {
    return sections.flatMap(s => s.quizzes).find(q => q.afterLessonId === lessonId);
  }

  getMessage(key: string, details?: any): string {
    const messages: Record<string, string> = {
      marked: `Lesson ${details.title} marked as completed successfully`,
      unmark: `Lesson ${details.title} marked as incomplete successfully`,
    };
    return messages[key];
  }

  setLessonType(lessonLink: string): void {
    const normalizedLink = lessonLink.trim().toLowerCase();  // Chuyển tất cả về chữ thường và loại bỏ khoảng trắng dư thừa
    if (normalizedLink.endsWith('.mp4') || normalizedLink.endsWith('.avi') || normalizedLink.endsWith('.mkv')) {
      this.lessonType = 'video'; // Video
    } else if (normalizedLink.endsWith('.docx')) {
      this.lessonType = 'docx'; // DOCX
    } else {
      console.warn('Unknown lesson type for link:', lessonLink);
    }
  }

  getQuizSubmissionByLessonId(lessonId: number, sections: Section[], quizSubmissions: QuizSubmission[]) {
    const quiz = sections.flatMap(s => s.quizzes).find(q => q.afterLessonId === lessonId);
    if (!quiz) {
      return null;
    }
    return quizSubmissions.find(qs => qs.quizId === quiz!.id);
  }

}
