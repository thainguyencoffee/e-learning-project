import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {DiscountService} from "../../service/discount.service";
import {updateForm} from "../../../../common/utils";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {EditCourseDto} from "../../../courses/model/edit-course.dto";
import {DiscountDto} from "../../model/discount-dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-edit-discount',
  standalone: true,
  imports: [
    InputRowComponent,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './edit-discount.component.html',
})
export class EditDiscountComponent implements OnInit {

  discountService = inject(DiscountService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler)

  currentId?: number;

  typesMap: Record<string, string> = {
    PERCENTAGE: 'Theo phần trăm',
    FIXED: 'Giảm cố định',
  }

  currenciesMap: Record<string, string> = {
    VND: 'VND',
  }

  discountTypeChange(key: string) {
    if (key === 'PERCENTAGE') {
      this.editForm.get('percentage')?.setValidators([Validators.required, Validators.min(1), Validators.max(100)]);
      this.editForm.get('fixedPrice')?.setValidators([]);
      this.editForm.get('currency')?.setValidators([]);

      // reset fixedPrice and currency
      this.editForm.get('fixedPrice')?.reset();
      this.editForm.get('currency')?.reset();
    } else {
      this.editForm.get('percentage')?.setValidators([]);
      this.editForm.get('fixedPrice')?.setValidators([Validators.required]);
      this.editForm.get('currency')?.setValidators([Validators.required]);

      // reset percentage
      this.editForm.get('percentage')?.reset();
    }

    this.editForm.get('percentage')?.updateValueAndValidity();
    this.editForm.get('fixedPrice')?.updateValueAndValidity();
    this.editForm.get('currency')?.updateValueAndValidity();
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    code: new FormControl(null, [Validators.required, Validators.maxLength(50), Validators.minLength(10), Validators.pattern(/^\S*$/)]),
    type: new FormControl(null, [Validators.required]),
    currency: new FormControl<string | null>(null, []),
    fixedPrice: new FormControl<string | null>(null, []),
    percentage: new FormControl(null, []),
    startDate: new FormControl(null, [Validators.required]),
    endDate: new FormControl(null, [Validators.required]),
    maxUsage: new FormControl(null, [Validators.required, Validators.min(1)]),
  });

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Discount was updated successfully.`
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.currentId = +this.route.snapshot.params['id'];
    this.discountService.getDiscount(this.currentId)
      .subscribe({
        next: (data) => {
          updateForm(this.editForm, data)
          if (data.fixedPrice) {
            const currency = data.fixedPrice.substring(0, 3) || null;
            this.editForm.get('currency')?.setValue(currency);

            const fixedPriceString = data.fixedPrice.substring(3) || null;
            const fixedPriceNumber = Number(fixedPriceString!.replace(/,/g, ''));
            this.editForm.get('fixedPrice')?.setValue(fixedPriceNumber + '');
          }

        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new DiscountDto(this.editForm.value);
    this.discountService.updateDiscount(this.currentId!, data)
      .subscribe({
        next: () => this.router.navigate(['/administration/discounts'], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });
  }


}
