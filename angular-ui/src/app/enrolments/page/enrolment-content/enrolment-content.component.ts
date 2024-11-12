import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink, RouterOutlet} from "@angular/router";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Observable, Subscription} from "rxjs";
import {EnrolmentsService} from "../../service/enrolments.service";
import {EnrolmentWithCourseDto} from "../../model/enrolment-with-course-dto";
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {EnrolmentWithCourseDataService} from "./enrolment-with-course-data.service";

@Component({
  selector: 'app-enrolment-content',
  standalone: true,
  imports: [
    NgForOf,
    NgClass,
    RouterLink,
    RouterOutlet,
    NgIf,
    AsyncPipe
  ],
  templateUrl: './enrolment-content.component.html',
})
export class EnrolmentContentComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);

  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.enrolmentId = +this.route.snapshot.params['id'];

    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => this.enrolmentWithCourseDataService.setEnrolmentWithCourse(data),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }

}
