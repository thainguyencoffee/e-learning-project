import {Component, inject, OnInit} from '@angular/core';
import {DiscountService} from "../../service/discount.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Course} from "../../../courses/model/view/course";
import {Subscription} from "rxjs";
import {Discount} from "../../model/view/discount";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-discount-trash',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink
  ],
  templateUrl: './discount-trash.component.html',
})
export class DiscountTrashComponent implements OnInit{

  discountService = inject(DiscountService);

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  discountsInTrash?: Discount[];
  size!: number;
  number!: number;
  totalElements!: number;
  totalPages!: number;
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete force this element?',
      confirmRestore: 'Do you really want to restore this element?',
      deleted: 'Course was removed successfully.',
      restored: 'Course was restored successfully.'
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
    this.discountService.getAllDiscountsInTrash(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.discountsInTrash = pageWrapper.content as Discount[];
          this.size = pageWrapper.page.size;
          this.number = pageWrapper.page.number;
          this.totalElements = pageWrapper.page.totalElements;
          this.totalPages = pageWrapper.page.totalPages;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmDeleteForce(discount: Discount) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.discountService.deleteDiscountForce(discount)
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

  confirmRestore(discountId: number) {
    if (confirm(this.getMessage('confirmRestore'))) {
      this.discountService.restoreDiscount(discountId)
        .subscribe({
          next: () => this.router.navigate(['/administration/discounts'], {
            state: {
              msgSuccess: this.getMessage('restored')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

}
