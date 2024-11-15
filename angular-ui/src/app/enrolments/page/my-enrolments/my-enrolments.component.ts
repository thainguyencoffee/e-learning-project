import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from "@angular/router";
import { ErrorHandler } from "../../../common/error-handler.injectable";
import { EnrolmentDTO } from "../../model/enrolment-dto";
import { PaginationUtils } from "../../../common/dto/page-wrapper";
import { Subscription } from "rxjs";
import { EnrolmentsService } from "../../service/enrolments.service";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import { CourseWithoutSections } from "../../../browse-course/model/course-without-sections";
import { EnrolmentWithCourseDto } from "../../model/enrolment-with-course-dto";

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    RouterLink,
    NgClass
  ],
  templateUrl: './my-enrolments.component.html',
})
export class MyEnrolmentsComponent implements OnInit, OnDestroy {

  enrollmentService = inject(EnrolmentsService);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);

  enrollments: EnrolmentDTO[] = [];
  groupedENnrol: { [completed: string]: EnrolmentDTO[] } = {}; // Sử dụng kiểu string làm key // Phân nhóm theo completed là boolean
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData(0);

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    });
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

  // Phân nhóm enrollments theo completed (true/false)
  groupEnrollmentsByCompletionStatus(): void {
    this.groupedENnrol = this.enrollments.reduce((grouped, enrollment) => {
      const status = enrollment.completed ? 'Completed' : 'Incomplete'; // Chuyển boolean thành string
      if (!grouped[status]) {
        grouped[status] = [];
      }
      grouped[status].push(enrollment);
      return grouped;
    }, {} as { [status: string]: EnrolmentDTO[] });
  }


  loadData(pageNumber: number): void {
    this.enrollmentService.getAllEnrollments(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.enrollments = pageWrapper.content as EnrolmentDTO[];

          // Phân nhóm lại sau khi tải dữ liệu
          this.groupEnrollmentsByCompletionStatus();
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  protected readonly Object = Object;
}
