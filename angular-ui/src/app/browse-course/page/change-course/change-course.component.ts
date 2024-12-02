import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {BrowseCourseService} from "../../service/browse-course.service";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Subscription} from "rxjs";
import {CourseCardComponent} from "../../../common/component/course-card/course-card.component";
import {NgForOf, NgIf} from "@angular/common";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {EnrollmentsService} from "../../../enrollment/service/enrollments.service";
import {Status, Type} from "../../model/change-course-response";

@Component({
  selector: 'app-change-course',
  standalone: true,
  imports: [
    CourseCardComponent,
    NgIf,
    NgForOf,
  ],
  templateUrl: './change-course.component.html',
})
export class ChangeCourseComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  browseCourseService = inject(BrowseCourseService);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);
  navigationSubscription?: Subscription;

  courseId?: number;
  enrollmentId?: number;
  currentCourse?: CourseWithoutSections;
  courses: CourseWithoutSections[] = [];
  paginationUtils?: PaginationUtils;

  ngOnInit(): void {
    this.loadData(0);

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  private loadData(pageNumber: number) {
    this.route.queryParams.subscribe(params => {
      this.courseId = params['courseId'];
      this.enrollmentId = params['enrollmentId'];
    });

    this.browseCourseService.getPublishedCourse(this.courseId!).subscribe({
      next: data => this.currentCourse = data,
      error: error => this.errorHandler.handleServerError(error.error)
    })

    this.browseCourseService.getAllPublishedCoursesWithPurchaseStatus(this.browseCourseService.getAllPublishedCourses(pageNumber)).subscribe({
      next: result => {
        this.courses = result.courses;
        this.paginationUtils = result.paginationUtils;
      },
      error: error => this.errorHandler.handleServerError(error.error)
    })

  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmChange: 'Do you really want to change this course?',
      additionalPayment: 'You need to pay additional fee to change this course.',
      changed: 'Course was changed successfully.',
    }
    return messages[key];
  }

  onChange(course: CourseWithoutSections) {
    if (confirm(this.getMessage('confirmChange'))) {
      this.enrollmentService.changeCourse(this.enrollmentId!, course.id)
        .subscribe({
          next: (changeCourseResponse) => {
            if (changeCourseResponse.type === Type.BASIC_CHANGE && changeCourseResponse.status === Status.SUCCESS) {
              this.router.navigate(['/courses', course.id], {
                state: {
                  msgSuccess: this.getMessage('changed')
                }
              })
            } else if (changeCourseResponse.type === Type.PENDING_PAYMENT_ADDITIONAL && changeCourseResponse.status === Status.PENDING) {
              this.router.navigate(['/checkout/pay', changeCourseResponse.orderId], {
                state: {
                  msgSuccess: this.getMessage('additionalPayment')
                }
              })
            }
          },
          error: error => this.errorHandler.handleServerError(error.error)
        })
    }
  }

}
