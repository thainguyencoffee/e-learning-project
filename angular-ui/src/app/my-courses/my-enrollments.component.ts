import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../common/error-handler.injectable";
import {CourseEnrollmentDTO} from "./model";
import {PaginationUtils} from "../administration/courses/model/view/page-wrapper";
import {Subscription} from "rxjs";
import {EnrollmentService} from "./enrollment.service";
import {DatePipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    RouterLink
  ],
  templateUrl: './my-enrollments.component.html',
})
export class MyEnrollmentsComponent implements OnInit, OnDestroy{

  enrollmentService = inject(EnrollmentService);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);

  enrollments: CourseEnrollmentDTO[] = [];
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData(0);

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
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

  loadData(pageNumber: number): void {
    this.enrollmentService.getAllEnrollments(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.enrollments = pageWrapper.content as CourseEnrollmentDTO[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

}
