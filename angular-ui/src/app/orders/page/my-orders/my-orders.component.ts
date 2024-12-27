import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {OrdersService} from "../../service/orders.service";
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {Order, OrderItem} from "../../model/order";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {forkJoin, map, Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {BrowseCourseService} from "../../../browse-course/service/browse-course.service";
import {shortUUID} from "../../../common/utils";

@Component({
  selector: 'app-my-orders',
  standalone: true,
    imports: [
        RouterLink,
        NgForOf,
        NgIf,
        DatePipe
    ],
  templateUrl: './my-orders.component.html',
})
export class MyOrdersComponent implements OnInit, OnDestroy {

  orderService = inject(OrdersService);
  browseCourseService = inject(BrowseCourseService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  errorHandler = inject(ErrorHandler);

  orders: Order[] = [];
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

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }


  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  loadData(pageNumber: number): void {
    this.orderService.getAllOrders(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          const orders = pageWrapper.content as Order[];
          forkJoin(
            orders.map(order =>
              forkJoin(
                order.items.map(item =>
                  this.browseCourseService.getPublishedCourse(item.course)
                    .pipe(map(course => ({...item, courseDetail: course})))
                )
              ).pipe(map(itemsWithDetails => ({ ...order, items: itemsWithDetails })))
            )
          ).subscribe({
            next: (ordersWithDetails) => {
              this.orders = ordersWithDetails;
            },
            error: (error) => this.errorHandler.handleServerError(error.error)
          })
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  getCourseIdAndTitle(items: OrderItem[]): {id: number, title: string} | null {
    if (items.length > 0) {
      return {
        id: items[0].courseDetail.id,
        title: items[0].courseDetail.title
      };
    }
    return null;
  }

  protected readonly shortUUID = shortUUID;
}
