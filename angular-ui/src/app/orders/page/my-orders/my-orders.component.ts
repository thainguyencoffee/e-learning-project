import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {OrdersService} from "../../service/orders.service";
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {Order} from "../../model/order";
import {PaginationUtils} from "../../../common/dto/page-wrapper";
import {Subscription} from "rxjs";
import {ErrorHandler} from "../../../common/error-handler.injectable";
import {DatePipe, NgForOf, NgIf} from "@angular/common";

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
          this.orders = pageWrapper.content as Order[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }


}
