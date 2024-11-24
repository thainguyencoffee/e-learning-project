import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {BrowseCourseService} from "../../service/browse-course.service";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Subscription} from "rxjs";
import {DatePipe, NgForOf, NgIf, SlicePipe} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {EnrollmentsService} from "../../../enrollment/service/enrollments.service";
import {Enrollment} from "../../../enrollment/model/enrollment";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {LoginComponent} from "../../../common/auth/login.component";
import {getStarsIcon} from "../../star-util";

@Component({
  selector: 'app-browse-course-detail',
  standalone: true,
  imports: [
    NgIf,
    RouterLink,
    LoginComponent,
    SlicePipe,
    DatePipe,
    NgForOf
  ],
  templateUrl: './browse-course-detail.component.html',
})
export class BrowseCourseDetailComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  browseCourseService = inject(BrowseCourseService);
  enrollmentService = inject(EnrollmentsService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  navigationSubscription?: Subscription;

  courseId?: number;
  course?: CourseWithoutSections;
  enrollment?: Enrollment;
  isAuthenticated= false;
  showFullDescription: boolean = false;

  ngOnInit(): void {
    this.loadData()

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  private loadData() {
    this.courseId = this.route.snapshot.params['id'];
    this.browseCourseService.getPublishedCourse(this.courseId!).subscribe({
      next: data => this.course = data,
      error: error => this.errorHandler.handleServerError(error.error)
    })

    this.userService.valueChanges.subscribe({
      next: user => {
        if (user.isAuthenticated) {
          this.isAuthenticated = true;
          this.getEnrollmentByCourseId()
        } else {
          this.isAuthenticated = false;
        }
      }
    })

  }

  private getEnrollmentByCourseId() {
    this.enrollmentService.getEnrollmentByCourseId(this.courseId!)
      .subscribe({
        next: data => {
          this.enrollment = data
        },
        error: _ => this.enrollment = undefined
      })
  }

  protected readonly getStarsIcon = getStarsIcon;
}
