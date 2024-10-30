import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {Course} from "../administration/courses/model/view/course";
import {BrowseCourseService} from "../browse-course.service";
import {ErrorHandler} from "../common/error-handler.injectable";
import {OrdersService} from "../orders/orders.service";
import {Observable, Subscription} from "rxjs";
import {AsyncPipe, CurrencyPipe, NgForOf, NgIf} from "@angular/common";
import {UserService} from "../common/auth/user.service";
import {LoginComponent} from "../common/auth/login.component";

@Component({
  selector: 'app-course-view',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    CurrencyPipe,
    LoginComponent,
    NgForOf,
    RouterLink
  ],
  templateUrl: './course-view.component.html',
})
export class CourseViewComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  browseCourseService = inject(BrowseCourseService);
  orderService = inject(OrdersService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  navigationSubscription?: Subscription;

  id?: number;
  course?: Course;
  hasPurchase$!: Observable<boolean>;

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
    this.id = this.route.snapshot.params['id'];
    this.browseCourseService.getPublishedCourse(this.id!).subscribe({
      next: data => this.course = data,
      error: error => this.errorHandler.handleServerError(error.error)
    })

    if (this.userService.current.isAuthenticated) {
      this.hasPurchase$ = this.orderService.hasPurchase(this.id!);
    } else {
      this.hasPurchase$ = new Observable<boolean>();
    }

  }

}
