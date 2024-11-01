import {Component, inject, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {DiscountService} from "../../service/discount.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {Discount} from "../../model/view/discount";
import {Subscription} from "rxjs";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-list-discount',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgForOf
  ],
  templateUrl: './list-discount.component.html'
})
export class ListDiscountComponent implements OnInit {

  discountService = inject(DiscountService);
  errorHandler = inject(ErrorHandler)
  router = inject(Router);

  discounts?: Discount[];
  size!: number;
  number!: number;
  totalElements!: number;
  totalPages!: number;
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to delete this discount?',
      deleted: 'Discount was removed successfully.',
    }
    return messages[key];
  }

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    const pageRange = [];
    for (let i = 0; i < this.totalPages; i++) {
      pageRange.push(i);
    }
    return pageRange;
  }

  loadData(pageNumber: number): void {
    this.discountService.getAllDiscounts(pageNumber)
      .subscribe({
        next: pageWrapper => {
          this.discounts = pageWrapper.content;
          this.size = pageWrapper.page.size;
          this.number = pageWrapper.page.number;
          this.totalElements = pageWrapper.page.totalElements;
          this.totalPages = pageWrapper.page.totalPages;
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  confirmDelete(id: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.discountService.deleteCourse(id)
        .subscribe({
          next: () => this.router.navigate(['/administration/discounts'], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }
}
