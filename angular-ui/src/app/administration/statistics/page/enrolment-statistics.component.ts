import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {EnrolmentStatisticService} from "../service/enrolment-statistic.service";
import {Subscription} from "rxjs";
import {CourseInfoWithEnrolmentStatisticDto} from "../model/course-info-with-enrolment-statistic.dto";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-enrolment-statistics',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf
  ],
  templateUrl: './enrolment-statistics.component.html',
})
export class EnrolmentStatisticsComponent implements OnInit, OnDestroy{

  enrolmentStatisticService = inject(EnrolmentStatisticService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  route = inject(ActivatedRoute);

  courseInfoWithEnrolmentStatistics?: CourseInfoWithEnrolmentStatisticDto[];
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
    this.enrolmentStatisticService.getCourseInfoWithEnrolmentStatisticDTO(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.courseInfoWithEnrolmentStatistics = pageWrapper.content as CourseInfoWithEnrolmentStatisticDto[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

}
