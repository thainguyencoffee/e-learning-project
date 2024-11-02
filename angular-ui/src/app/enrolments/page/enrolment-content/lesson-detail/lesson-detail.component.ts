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
import {LessonProgress} from "../../../model/lesson-progress";

@Component({
  selector: 'app-lesson-detail',
  standalone: true,
  imports: [
    RouterLink,
    AsyncPipe,
    NgIf,
    VideoPlayerComponent
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

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      marked: `Lesson ${details.title} marked as completed successfully`,
      unmark: `Lesson ${details.title} marked as incomplete successfully`,
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.loadData();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })

  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  loadData() {
    this.enrolmentId = +this.route.snapshot.params['enrolmentId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.route.queryParams.subscribe(params => {
      this.lessonPrev = +params['lessonPrev'];
      this.lessonNext = +params['lessonNext'];
    });

    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => this.enrolmentWithCourseDataService.setEnrolmentWithCourse(data),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  getLessonById(lessonId: number, sections: Section[]) {
    return sections.flatMap(s => s.lessons).find(l => l.id === lessonId);
  }

  lessonIsCompleted(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.some(lp => lp.lessonId === lessonId && lp.completed);
  }

  markLessonAsCompleted(lessonId: number, title: string) {
    this.enrolmentService.markLessonAsCompleted(this.enrolmentId!, lessonId)
      .subscribe({
        next: _ => {
          const queryParams = this.route.snapshot.queryParams;
          this.router.navigate(['.'],
            {
              relativeTo: this.route,
              queryParams: queryParams,
              state: {
                msgSuccess: this.getMessage('marked', {title})
              }
            })
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  markLessonAsIncomplete(lessonId: number, title: string) {
    this.enrolmentService.markLessonAsIncomplete(this.enrolmentId!, lessonId)
      .subscribe({
        next: _ => {
          const queryParams = this.route.snapshot.queryParams;
          this.router.navigate(['.'],
            {
              relativeTo: this.route,
              queryParams: queryParams,
              state: {
                msgSuccess: this.getMessage('unmark', {title})
              }
            })
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

}
