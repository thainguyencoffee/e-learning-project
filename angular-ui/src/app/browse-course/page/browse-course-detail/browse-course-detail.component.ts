import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {BrowseCourseService} from "../../service/browse-course.service";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {Observable, Subscription} from "rxjs";
import {AsyncPipe, CurrencyPipe, NgIf} from "@angular/common";
import {UserService} from "../../../common/auth/user.service";
import {EnrolmentsService} from "../../../enrolments/service/enrolments.service";
import {Enrolment} from "../../../enrolments/model/enrolment";
import {CourseWithoutSections} from "../../model/course-without-sections";
import {LoginComponent} from "../../../common/auth/login.component";

@Component({
  selector: 'app-browse-course-detail',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    CurrencyPipe,
    RouterLink,
    LoginComponent
  ],
  templateUrl: './browse-course-detail.component.html',
})
export class BrowseCourseDetailComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  browseCourseService = inject(BrowseCourseService);
  enrolmentService = inject(EnrolmentsService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  navigationSubscription?: Subscription;

  courseId?: number;
  course?: CourseWithoutSections;
  enrolment?: Enrolment;
  isAuthenticated= false;

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
          this.getEnrolmentByCourseId()
        } else {
          this.isAuthenticated = false;
        }
      }
    })

  }

  private getEnrolmentByCourseId() {
    this.enrolmentService.getEnrolmentByCourseId(this.courseId!)
      .subscribe({
        next: data => {
          this.enrolment = data
        },
        error: _ => this.enrolment = undefined
      })
  }

}
