import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {EnrollmentWithCourseDataService} from "../enrollment-with-course-data.service";
import {Observable, Subscription} from "rxjs";
import {EnrollmentWithCourseDto} from "../../../model/enrollment-with-course-dto";
import {AsyncPipe, NgClass, NgIf} from "@angular/common";
import {Section} from "../../../../administration/courses/model/view/section";
import {EnrollmentsService} from "../../../service/enrollments.service";
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
    RouterLink,
    NgClass
  ],
  templateUrl: './lesson-detail.component.html',
})
export class LessonDetailComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentWithCourseDataService = inject(EnrollmentWithCourseDataService);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);

  lessonId?: number;
  lessonPrev?: number;
  lessonNext?: number;
  enrollmentId?: number;
  enrollmentWithCourse$!: Observable<EnrollmentWithCourseDto | null>;
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
    this.enrollmentId = +this.route.snapshot.params['enrollmentId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.route.queryParams.subscribe(params => {
      this.lessonPrev = +params['lessonPrev'];
      this.lessonNext = +params['lessonNext'];
    });

    this.enrollmentService.getEnrollmentWithCourseByEnrollmentId(this.enrollmentId)
      .subscribe({
        next: (data) => {
          const sortedData = this.enrollmentWithCourseDataService.sortSectionAndLessons(data);
          this.enrollmentWithCourseDataService.setEnrollmentWithCourse(sortedData);


          const lesson = this.getLessonById(this.lessonId!, sortedData.sections);
          if (lesson && lesson.link) {
            this.setLessonType(lesson.link);
          }
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });

    this.enrollmentWithCourse$ = this.enrollmentWithCourseDataService.enrollmentWithCourse$;
  }

  getLessonById(lessonId: number, sections: Section[]) {
    return sections.flatMap(s => s.lessons).find(l => l.id === lessonId);
  }

  lessonIsCompleted(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.some(lp => lp.lessonId === lessonId && lp.completed);
  }

  isLessonBonus(lessonId: number, lessonProgresses: LessonProgress[]) {
    const lessonProgress = lessonProgresses.find(lp => lp.lessonId === lessonId);
    return lessonProgress ? lessonProgress.bonus : true;
  }

  markLessonAsCompleted(courseId: number, lessonId: number, sections: Section[], quizSubmissions: QuizSubmission[]) {
    const quiz = this.getQuizByLessonId(lessonId, sections);
    this.enrollmentService.markLessonAsCompleted(this.enrollmentId!, courseId, lessonId).subscribe({
      next: _ => {
        this.loadData();
        if (quiz) {
          const quizSubmission = this.getQuizSubmissionByLessonId(lessonId, sections, quizSubmissions);
          if (!quizSubmission) {
            this.router.navigate(['/enrollments', this.enrollmentId, 'quiz-submit', quiz.id]);
          }
        }
      },
      error: error => {
        this.errorHandler.handleServerError(error.error);
        return;
      },
    });

  }

  markLessonAsIncomplete(courseId: number, lessonId: number, title: string): void {
    this.enrollmentService.markLessonAsIncomplete(this.enrollmentId!, courseId, lessonId)
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
    if (lessonLink.endsWith('.mp4') || lessonLink.endsWith('.avi') || lessonLink.endsWith('.mkv')) {
      this.lessonType = 'video';
    } else if (lessonLink.endsWith('.docx')) {
      this.lessonType = 'docx';
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

  inProgress(lessonId: number, lessonProgresses: LessonProgress[]) {
    const lessonProgress = lessonProgresses.find(lp => lp.lessonId === lessonId);
    return lessonProgress ? lessonProgress.inProgress : false;
  }

}
