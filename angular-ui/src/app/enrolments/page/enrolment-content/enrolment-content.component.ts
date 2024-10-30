import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Subscription} from "rxjs";
import {EnrolmentsService} from "../../service/enrolments.service";
import {EnrolmentWithCourseDto} from "../../model/enrolment-with-course-dto";

@Component({
  selector: 'app-enrolment-content',
  standalone: true,
  imports: [],
  templateUrl: './enrolment-content.component.html',
})
export class EnrolmentContentComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  enrolmentId?: number;
  enrolmentWithCourse?: EnrolmentWithCourseDto;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }

  loadData(): void {
    this.enrolmentId = +this.route.snapshot.params['id'];

    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => this.enrolmentWithCourse = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  protected readonly JSON = JSON;
}
