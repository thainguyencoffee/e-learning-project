import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {BrowseCourseService} from "../../browse-course.service";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Course} from "../../administration/courses/model/view/course";
import {createErrNotFoundByProperty, ErrorHandler} from "../../common/error-handler.injectable";
import {Subscription} from "rxjs";
import {NgForOf, NgIf} from "@angular/common";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DiscountService} from "../../administration/discounts/service/discount.service";
import {Discount} from "../../administration/discounts/model/view/discount";
import {InputRowComponent} from "../../common/input-row/input-row.component";
import {calcDifference, calcMultiplier} from "../../common/utils";
import {OrdersService} from "../orders.service";
import {OrderRequestDto} from "../order-request.dto";

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    ReactiveFormsModule,
    InputRowComponent
  ],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnInit, OnDestroy {

  browseCourseService = inject(BrowseCourseService);
  discountService = inject(DiscountService);
  checkoutService = inject(OrdersService);
  route = inject(ActivatedRoute);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courseIds: number[] = [];
  courses: Course[] = [];
  navigationSubscription?: Subscription;

  selectedCourse?: Course;
  discountCode?: string = '';
  discountSelected?: Discount;

  discountCodeForm = new FormGroup({
    discountCode: new FormControl(null, [])
  })

  ngOnInit(): void {
    const courseId = +this.route.snapshot.params['courseId']; // this is for the route /checkout/:courseId
    this.courseIds.push(courseId);

    this.loadData(this.courseIds);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(this.courseIds);
      }
    })

  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  loadData(courseIds: number[]) {
    for (let courseId of courseIds) {
      this.browseCourseService.getPublishedCourse(courseId)
        .subscribe({
          next: course => this.courses!.push(course),
          error: error => this.errorHandler.handleServerError(error.error)
        })
    }
    this.calcTotalPrice();
  }

  selectCourse(course: Course) {
    this.selectedCourse = course;
  }

  clearSelected() {
    this.selectedCourse = undefined;
  }

  handleSearchForm() {
    window.scrollTo(0, 0);
    this.discountCodeForm.markAllAsTouched();
    if (!this.discountCodeForm.valid) {
      return;
    }

    if (this.discountCodeForm.value.discountCode) {
      this.discountService.getDiscountByCode(this.discountCodeForm.value.discountCode).subscribe({
        next: discount => {
          this.applyDiscount(discount);
        },
        error: (error) => {
          if (error.status === 404) {
            this.applyDiscount(undefined);
            this.errorHandler.handleServerError(
              createErrNotFoundByProperty("discountCode", "Discount code not found"), this.discountCodeForm)
          } else {
            this.errorHandler.handleServerError(error.error)
          }
        }
      })
    }
  }

  applyDiscount(discount?: Discount) {
    this.discountSelected = discount;
  }

  calcTotalPrice(): string {
    const [total, currency] = this.courses.reduce(
      (acc, course) => {
        if (course.price) {
          const numericPrice = parseFloat(course.price.substring(3).replace(/,/g, ''));
          acc[0] += numericPrice;
          acc[1] = acc[1] || course.price.substring(0, 3);
        }
        return acc;
      },
      [0, '']
    );

    return `${currency}${total.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
  }

  calcDifference(price1: string, price2: string) {
    return calcDifference(price1, price2);
  }

  calcMultiplier(price: string, multiplier: number) {
    return calcMultiplier(price, multiplier);
  }

  clearDiscount() {
    this.discountSelected = undefined
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to enroll this course?',
      created: 'Order was created successfully.',
    }
    return messages[key];
  }

  confirmEnrollCourse() {
    const data = new OrderRequestDto({
      items: this.courseIds.map(id => ({id})),
      discountCode: this.discountSelected?.code
    })

    if (confirm(this.getMessage('confirm'))) {
      this.checkoutService.createOrder(data)
        .subscribe({
          next: (order) => {
            const orderId = order.id;
            this.router.navigate(['/checkout/pay', orderId], {
              state: {
                msgSuccess: this.getMessage('created')
              }
            })
          },
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }

  }

}
