import {AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BrowseCourseService} from "../../service/browse-course.service";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {NgForOf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {OrdersService} from "../../../orders/service/orders.service";
import {CourseWithoutSections} from "../../model/course-without-sections";
import KeenSlider, {KeenSliderInstance} from "keen-slider"


@Component({
  selector: 'app-browse-courses',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink
  ],
  templateUrl: './browse-courses.component.html',
  styleUrls: ["browse-courses.component.css"]
})
export class BrowseCoursesComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild("sliderRef", {static: true}) sliderRef!: ElementRef<HTMLElement>;
  slider: KeenSliderInstance | null = null;

  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courseWithoutSectionsList: CourseWithoutSections[] = [];
  groupedCourses: { [language: string]: CourseWithoutSections[] } = {};  // New property for grouped courses
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

  loadData(pageNumber: number): void {
    this.browseCourseService.getAllPublishedCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {

          if (this.userService.current.isAuthenticated) {
            this.orderService.getAllCourseIdsHasPurchased().subscribe({
              next: (purchasedCourses) => {
                const allCourses = pageWrapper.content as CourseWithoutSections[];
                this.courseWithoutSectionsList = allCourses.filter(course => !purchasedCourses.includes(course.id));
                this.groupCoursesByLanguage();
              }
            });
          } else {
            this.courseWithoutSectionsList = pageWrapper.content as CourseWithoutSections[];
            this.groupCoursesByLanguage();
          }

          this.paginationUtils = new PaginationUtils(pageWrapper.page);

        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  private groupCoursesByLanguage(): void {
    this.groupedCourses = this.courseWithoutSectionsList.reduce((groups, course) => {
      const language = course.language || 'Other';
      if (!groups[language]) {
        groups[language] = [];
      }
      groups[language].push(course);
      return groups;
    }, {} as { [language: string]: CourseWithoutSections[] });
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
    if (this.slider) this.slider.destroy()
  }

  ngAfterViewInit() {
    this.slider = new KeenSlider(this.sliderRef.nativeElement, {
      loop: true,
      slides: {perView: 4, spacing: 15},
      breakpoints: {
        "(max-width: 768px)": {slides: {perView: 2}},
        "(max-width: 1024px)": {slides: {perView: 2}},
      },
    });
  }

  chunkCourses(courses: any[], chunkSize: number): any[][] {
    const chunks = [];
    for (let i = 0; i < courses.length; i += chunkSize) {
      chunks.push(courses.slice(i, i + chunkSize));
    }
    return chunks;
  }

  protected readonly Object = Object;
}
