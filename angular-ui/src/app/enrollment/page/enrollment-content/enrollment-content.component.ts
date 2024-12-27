import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterOutlet} from "@angular/router";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {catchError, map, Observable, of, switchMap} from "rxjs";
import {EnrollmentsService} from "../../service/enrollments.service";
import {EnrollmentWithCourseDto} from "../../model/enrollment-with-course-dto";
import {AsyncPipe, DatePipe, NgClass, NgForOf, NgIf, SlicePipe} from "@angular/common";
import {EnrollmentWithCourseDataService} from "./enrollment-with-course-data.service";
import {CoursePathPublishedService} from "../../../administration/course-path/service/course-path-published.service";
import {
  CourseOrderPublishedDto,
  CoursePathPublishedDto
} from "../../../administration/course-path/model/course-path-published.dto";
import {PaginationUtils} from "../../../common/dto/page-wrapper";

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
    DatePipe,
    NgForOf
  ],
  templateUrl: './enrollment-content.component.html',
})
export class EnrollmentContentComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentService = inject(EnrollmentsService);
  coursePathPublishedService = inject(CoursePathPublishedService);
  errorHandler = inject(ErrorHandler);

  enrollmentWithCourseDataService = inject(EnrollmentWithCourseDataService);

  enrollmentId?: number;
  enrollmentWithCourse?: EnrollmentWithCourseDto | null;
  // enrollmentWithCourse$!: Observable<EnrollmentWithCourseDto | null>;

  coursePathsPublished?: CoursePathPublishedDto[];
  purchaseCourseIds: number[] = [];
  paginationUtils?: PaginationUtils;

  ngOnInit(): void {
    this.loadData(0);
  }

  loadData(pageNumber: number): void {
    this.enrollmentId = +this.route.snapshot.params['id'];

    this.enrollmentService.getEnrollmentWithCourseByEnrollmentId(this.enrollmentId)
      .subscribe({
        next: (data) => {
          this.enrollmentWithCourseDataService.setEnrollmentWithCourse(data);
          this.enrollmentWithCourse = data;

          this.fetchCoursePathPublished(this.enrollmentWithCourse.courseId, pageNumber);
          this.fetchPurchaseCourseIds();
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  private fetchCoursePathPublished(courseId: number, pageNumber: number) {
    this.coursePathPublishedService.getPublishedCoursePaths(courseId, pageNumber)
      .subscribe({
        next: pageWrapper => {
          this.coursePathsPublished = pageWrapper.content as CoursePathPublishedDto[];
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  private fetchPurchaseCourseIds() {
    this.enrollmentService.getPurchasedCourses().subscribe({
      next: data => this.purchaseCourseIds= data,
      error: (error) => this.errorHandler.handleServerError(error.error)
    })
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }

  courseOrdersSorted(courseOrders: CourseOrderPublishedDto[]) {
    return courseOrders.sort((a, b) => a.orderIndex - b.orderIndex);
  }

  isTheBestSeller(coursePath: CoursePathPublishedDto, courseOrder: CourseOrderPublishedDto) {
    const bestSeller = coursePath.courseOrders.reduce((prev, current) =>
      prev.orderIndex > current.orderIndex ? prev : current);
    console.log(bestSeller)
    return bestSeller.id === courseOrder.id;
  }

  isEnrolled(courseId: number) {
    return this.enrollmentService.getEnrollmentByCourseId(courseId).pipe(
      map((enrollment) => {
        return !!enrollment;
      }),
      catchError((error) => {
        return of(false);
      })
    );
  }

}
