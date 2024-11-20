import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import {ActivatedRoute, Router, NavigationEnd, RouterLink} from '@angular/router';
import { EnrolmentWithCourseDataService } from '../enrolment-with-course-data.service';
import { Observable, Subscription } from 'rxjs';
import { EnrolmentWithCourseDto } from '../../../model/enrolment-with-course-dto';
import { AsyncPipe, NgIf } from '@angular/common';
import { Section } from '../../../../administration/courses/model/view/section';
import { EnrolmentsService } from '../../../service/enrolments.service';
import { ErrorHandler } from '../../../../common/error-handler.injectable';
import { VideoPlayerComponent } from '../../../../common/video-player/video-player.component';
import { DocumentViewerComponent } from '../../../../common/documentviewer/document-viewer.component';
import { LessonProgress } from '../../../model/lesson-progress';

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

    // Subscribe to router events for navigation changes
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe(); // Unsubscribe to prevent memory leaks
    }
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

  markLessonAsCompleted(lessonId: number, title: string, sections: Section[]) {
    const quiz = this.getQuizByLessonId(lessonId, sections);
    this.completeLesson(lessonId, title);

    if (quiz) {
      this.enrolmentService.isSubmittedQuiz(this.enrolmentId!, quiz.id).subscribe({
        next: isQuizSubmitted => {
          if (!isQuizSubmitted) {
            console.log('Quiz not submitted yet');
            this.router.navigate(['/enrolments', this.enrolmentId, 'quiz-submit', quiz.id], {
              queryParams: { returnUrl: this.router.url }
            });
          }
        },
        error: error => this.errorHandler.handleServerError(error.error),
      });
    }
  }

  private completeLesson(lessonId: number, title: string): void {
    this.enrolmentService.markLessonAsCompleted(this.enrolmentId!, lessonId).subscribe({
      next: () => this.loadData(),
      error: error => this.errorHandler.handleServerError(error.error),
    });
  }

  markLessonAsIncomplete(lessonId: number, title: string): void {
    this.enrolmentService.markLessonAsIncomplete(this.enrolmentId!, lessonId)
      .subscribe({
        next: () => {
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

  // Kiểm tra kiểu tài liệu là video hay docx
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
}
