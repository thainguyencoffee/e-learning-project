import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../../service/browse-course.service";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {NgClass, NgForOf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {OrdersService} from "../../../orders/service/orders.service";
import {CourseWithoutSections} from "../../model/course-without-sections";

@Component({
  selector: 'app-browse-courses',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink,
    NgClass
  ],
  templateUrl: './browse-courses.component.html',
})
export class BrowseCoursesComponent implements OnInit, OnDestroy {

  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courseWithoutSectionsList: CourseWithoutSections[] = [];
  paginationUtils = {
    number: 0,  // Current page
    totalPages: 0  // Total number of pages
  };
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  loadData(pageNumber: number): void {
    this.browseCourseService.getAllPublishedCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {

          if (this.userService.current.isAuthenticated) {
            this.orderService.getAllCourseIdsHasPurchased().subscribe({
              next: (purchasedCourses) => {
                const allCourses = pageWrapper.content as CourseWithoutSections[];
                this.courseWithoutSectionsList = allCourses.filter(course => !purchasedCourses.includes(course.id))
              }
            })
          } else {
            this.courseWithoutSectionsList = pageWrapper.content as CourseWithoutSections[];
          }

          this.paginationUtils = new PaginationUtils(pageWrapper.page);

        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  get paginatedCourses(): any[] {
    const startIndex = this.paginationUtils.number * 12;
    const endIndex = startIndex + 12;
    return this.courseWithoutSectionsList.slice(startIndex, endIndex);
  }

  // Method to handle page changes
  onPageChange(page: number): void {
    if (page >= 0 && page < this.paginationUtils.totalPages) {
      this.paginationUtils.number = page;
    }
  }

  // Method to get range of page numbers for pagination buttons
  getPageRange(): number[] {
    const range = [];
    for (let i = 0; i < this.paginationUtils.totalPages; i++) {
      range.push(i);
    }
    return range;
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
  }

}
