import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../../service/browse-course.service";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {PageWrapper, PaginationUtils} from "../../../common/dto/page-wrapper";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {NgForOf, NgIf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {OrdersService} from "../../../orders/service/orders.service";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {getStarsIcon} from "../../star-util";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-browse-courses',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink,
    FormsModule,
    NgIf,
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
      this.browseCourseService.searchPublishedCourses(this.searchQuery, pageNumber)
        .subscribe({
          next: pageWrapper => {
            this.handleData(pageWrapper);
          },
          error: (error) => this.errorHandler.handleServerError(error.error)
        })
    } else {
      this.loadData(0);
    }
  }

  loadData(pageNumber: number): void {
    this.browseCourseService.getAllPublishedCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.handleData(pageWrapper);
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
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

  protected readonly getStarsIcon = getStarsIcon;


  handleData(pageWrapper: PageWrapper<CourseWithoutSections>) {
    this.paginationUtils = new PaginationUtils(pageWrapper.page);
    if (this.userService.current.isAuthenticated) {
      this.orderService.getAllCourseIdsHasPurchased().subscribe({
        next: (purchasedCourses) => {
          const allCourses = pageWrapper.content as CourseWithoutSections[];
          this.courses = this.markListAsPurchased(allCourses, purchasedCourses);
        }
      })
    } else {
      this.courses = pageWrapper.content as CourseWithoutSections[];
    }
  }

  private markListAsPurchased(allCourses: CourseWithoutSections[], purchasedCourses: number[]) {
    return allCourses.map(course => {
      course.hasPurchased = purchasedCourses.includes(course.id);
      return course;
    })
  }

}
