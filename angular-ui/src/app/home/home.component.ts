import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../browse-course.service";
import {Course} from "../administration/courses/model/view/course";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../common/error-handler.injectable";
import {PaginationUtils} from "../administration/courses/model/view/page-wrapper";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {NgForOf} from "@angular/common";
import {UserService} from "../common/auth/user.service";
import {OrdersService} from "../orders/orders.service";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink
  ],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit, OnDestroy {

  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courses: Course[] = [];
  paginationUtils?: PaginationUtils
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
            this.orderService.purchasedCourses().subscribe({
              next: (purchasedCourses) => {
                const allCourses = pageWrapper.content as Course[];
                this.courses = allCourses.filter(course => !purchasedCourses.includes(course.id))
              }
            })
          } else {
            this.courses = pageWrapper.content as Course[];
          }

          this.paginationUtils = new PaginationUtils(pageWrapper.page);

        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
  }

}
