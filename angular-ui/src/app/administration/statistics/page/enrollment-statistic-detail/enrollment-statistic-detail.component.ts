import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {EnrollmentStatisticService} from "../../service/enrollment-statistic.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {CourseInfoWithEnrollmentsDto} from "../../model/course-info-with-enrollments.dto";
import {Subscription} from "rxjs";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";
import {EnrollmentsService} from "../../../../enrollment/service/enrollments.service";

@Component({
  selector: 'app-enrollment-statistic-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgForOf,
    DatePipe
  ],
  templateUrl: './enrollment-statistic-detail.component.html',
})
export class EnrollmentStatisticDetailComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentStatisticService = inject(EnrollmentStatisticService);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);
  userService = inject(UserService);

  courseId?: number;
  courseInfoWithEnrollments?: CourseInfoWithEnrollmentsDto;
  navigationSubscription?: Subscription;

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

  private loadData() {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.enrollmentStatisticService.getCourseInfoWithEnrollments(this.courseId)
      .subscribe({
        next: data => this.courseInfoWithEnrollments = data,
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  isCreateByYou(teacher: string) {
    return teacher === this.userService.current.name;
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Are you sure you want to delete this quiz submission?. Students will lose their progress. This action cannot be undone.',
      deleted: 'Quiz submission has been deleted successfully.',
    }
    return messages[key];
  }

  deleteQuizSubmission(enrollmentId: number, quizSubmissionId: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.enrollmentService.deleteSubmission(enrollmentId, quizSubmissionId)
        .subscribe({
          next: () => {
            this.router.navigate(['.'], {
              relativeTo: this.route,
              state: {
                msgSuccess: this.getMessage('deleted')
              }
            })
          },
          error: error => this.errorHandler.handleServerError(error.error)
        });
    }
  }

}
