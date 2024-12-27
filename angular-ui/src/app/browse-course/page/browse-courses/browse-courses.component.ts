import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../../service/browse-course.service";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {NavigationEnd, Router} from "@angular/router";
import {NgForOf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {OrdersService} from "../../../orders/service/orders.service";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {FormsModule} from "@angular/forms";
import {CourseCardComponent} from "../../../common/component/course-card/course-card.component";

@Component({
  selector: 'app-browse-courses',
  standalone: true,
  imports: [
    NgForOf,
    FormsModule,
    CourseCardComponent,
  ],
  templateUrl: './browse-courses.component.html',
})
export class BrowseCoursesComponent implements OnInit, OnDestroy {

  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courses: CourseWithoutSections[] = [];
  paginationUtils?: PaginationUtils = new PaginationUtils({
    number: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  navigationSubscription?: Subscription;

  searchQuery?: string;

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  onSearch(pageNumber: number = 0) {
    if (this.searchQuery && this.searchQuery.trim()) {
      this.browseCourseService.getAllPublishedCoursesWithPurchaseStatus(this.browseCourseService.searchPublishedCourses(this.searchQuery, pageNumber))
        .subscribe({
          next: result => {
            this.courses = result.courses;
            this.paginationUtils = result.paginationUtils;
          },
          error: (error) => this.errorHandler.handleServerError(error.error)
        })
    } else {
      this.loadData(0);
    }
  }

  loadData(pageNumber: number): void {
    this.browseCourseService.getAllPublishedCoursesWithPurchaseStatus(this.browseCourseService.getAllPublishedCourses(pageNumber))
      .subscribe({
        next: result => {
          this.courses = result.courses;
          this.paginationUtils = result.paginationUtils;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      if (this.searchQuery && this.searchQuery.trim()) {
        this.onSearch(pageNumber);
      } else {
        this.loadData(pageNumber);
      }
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
  }

}
