import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterOutlet} from "@angular/router";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Observable} from "rxjs";
import {EnrollmentsService} from "../../service/enrollments.service";
import {EnrollmentWithCourseDto} from "../../model/enrollment-with-course-dto";
import {AsyncPipe, DatePipe, NgClass, NgIf, SlicePipe} from "@angular/common";
import {EnrollmentWithCourseDataService} from "./enrollment-with-course-data.service";

@Component({
  selector: 'app-enrollment-content',
  standalone: true,
  imports: [
    NgClass,
    RouterLink,
    RouterOutlet,
    NgIf,
    AsyncPipe,
    SlicePipe,
    DatePipe
  ],
  templateUrl: './enrollment-content.component.html',
})
export class EnrollmentContentComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);
  enrollmentWithCourseDataService = inject(EnrollmentWithCourseDataService);

  enrollmentId?: number;
  enrollmentWithCourse$!: Observable<EnrollmentWithCourseDto | null>;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.enrollmentId = +this.route.snapshot.params['id'];

    this.enrollmentService.getEnrollmentWithCourseByEnrollmentId(this.enrollmentId)
      .subscribe({
        next: (data) => this.enrollmentWithCourseDataService.setEnrollmentWithCourse(data),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })

    this.enrollmentWithCourse$ = this.enrollmentWithCourseDataService.enrollmentWithCourse$;
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }

}
