import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../../service/browse-course.service";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {NgForOf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {OrdersService} from "../../../orders/service/orders.service";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {getStarsIcon} from "../../star-util";

@Component({
  selector: 'app-browse-courses',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink,
  ],
  templateUrl: './browse-courses.component.html',
  styleUrl: 'browse-courses.component.css'
})
export class BrowseCoursesComponent implements OnInit, OnDestroy {

  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courseWithoutSectionsList: CourseWithoutSections[] = [];
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

  loadData(pageNumber: number): void {
    this.browseCourseService.getAllPublishedCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
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

  protected readonly getStarsIcon = getStarsIcon;
}
