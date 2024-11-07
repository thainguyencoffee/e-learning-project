import { KeyValuePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import {getGlobalErrorMessage} from "../error-handler.injectable";


@Component({
  selector: 'app-input-errors',
  standalone: true,
  template: `
    @if (control?.invalid && (control?.dirty || control?.touched)) {
      <div class="invalid-feedback d-block">
        @for (error of control?.errors | keyvalue; track error.key) {
          <div class="mb-0">
            @if (isString(error.value)) {
              {{ error.value }}
            } @else {
              {{ getMessage(error.key, error.value) }}
            }
          </div>
        }
      </div>
    }
  `,
  imports: [KeyValuePipe]
})
export class InputErrorsComponent {

  @Input({ required: true })
  control?: AbstractControl;

  isString(value: any): boolean {
    return typeof value === 'string';
  }

  getMessage(key: string, details?: any) {
    const globalErrorMessage = getGlobalErrorMessage(key, details);
    return globalErrorMessage || key;
  }

}
