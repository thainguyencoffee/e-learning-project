import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {EnrolmentStatisticService} from "../../service/enrolment-statistic.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {CourseInfoWithEnrolmentsDto} from "../../model/course-info-with-enrolments.dto";
import {Subscription} from "rxjs";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";
import {EnrolmentsService} from "../../../../enrolments/service/enrolments.service";

@Component({
  selector: 'app-enrolment-statistic-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgForOf,
    DatePipe
  ],
  templateUrl: './enrolment-statistic-detail.component.html',
})
export class EnrolmentStatisticDetailComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentStatisticService = inject(EnrolmentStatisticService);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);
  userService = inject(UserService);

  courseId?: number;
  courseInfoWithEnrolments?: CourseInfoWithEnrolmentsDto;
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
    this.enrolmentStatisticService.getCourseInfoWithEnrolments(this.courseId)
      .subscribe({
        next: data => this.courseInfoWithEnrolments = data,
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

  deleteQuizSubmission(enrolmentId: number, quizSubmissionId: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.enrolmentService.deleteSubmission(enrolmentId, quizSubmissionId)
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
