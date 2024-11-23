import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {EnrollmentStatisticService} from "../service/enrollment-statistic.service";
import {Subscription} from "rxjs";
import {CourseInfoWithEnrollmentStatisticDto} from "../model/course-info-with-enrollment-statistic.dto";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-enrollment-statistics',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf
  ],
  templateUrl: './enrollment-statistics.component.html',
})
export class EnrollmentStatisticsComponent implements OnInit, OnDestroy{

  enrollmentStatisticService = inject(EnrollmentStatisticService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  route = inject(ActivatedRoute);

  courseInfoWithEnrollmentStatistics?: CourseInfoWithEnrollmentStatisticDto[];
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
    this.enrollmentStatisticService.getCourseInfoWithEnrollmentStatisticDTO(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.courseInfoWithEnrollmentStatistics = pageWrapper.content as CourseInfoWithEnrollmentStatisticDto[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

}
