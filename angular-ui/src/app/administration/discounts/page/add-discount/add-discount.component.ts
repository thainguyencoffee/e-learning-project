import {Component, inject, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from "@angular/forms";
import {DiscountService} from "../../service/discount.service";
import {DiscountDto} from "../../model/discount-dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-add-discount',
  standalone: true,
  imports: [
    FormsModule,
    InputRowComponent,
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './add-discount.component.html',
})
export class AddDiscountComponent {

  router = inject(Router);
  discountService = inject(DiscountService);
  errorHandler = inject(ErrorHandler);

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Discount was created successfully.`,
      pattern: 'Code must not contain any whitespace',
    };
    return messages[key];
  }

  typesMap: Record<string, string> = {
    PERCENTAGE: 'Percentage',
    FIXED: 'Fixed',
  }

  currenciesMap: Record<string, string> = {
    VND: 'VND',
  }

  discountTypeChange(key: string) {
    if (key === 'PERCENTAGE') {
      this.addForm.get('percentage')?.setValidators([Validators.required, Validators.min(1), Validators.max(100)]);
      this.addForm.get('maxValue')?.setValidators([Validators.required]);
      this.addForm.get('currency')?.setValidators([Validators.required]);

      this.addForm.get('fixedPrice')?.setValidators([]);
    } else {
      this.addForm.get('percentage')?.setValidators([]);
      this.addForm.get('maxValue')?.setValidators([]);

      this.addForm.get('fixedPrice')?.setValidators([Validators.required]);
      this.addForm.get('currency')?.setValidators([Validators.required]);
    }

    this.addForm.get('percentage')?.updateValueAndValidity();
    this.addForm.get('fixedPrice')?.updateValueAndValidity();
    this.addForm.get('currency')?.updateValueAndValidity();
  }

  addForm = new FormGroup({
    code: new FormControl(null, [Validators.required, Validators.maxLength(50), Validators.minLength(10), Validators.pattern(/^\S*$/)]),
    type: new FormControl(null, [Validators.required]),
    currency: new FormControl(null, [Validators.required]),
    percentage: new FormControl(null, []),
    maxValue: new FormControl(null, []),
    fixedPrice: new FormControl(null, []),
    startDate: new FormControl(null, [Validators.required]),
    endDate: new FormControl(null, [Validators.required]),
    maxUsage: new FormControl(null, [Validators.required, Validators.min(1)]),
  });

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    const data = new DiscountDto(this.addForm.value);
    console.log(data)

    this.discountService.createDiscount(data).subscribe({
      next:() => this.router.navigate(['/administration/discounts'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    })
  }

}
