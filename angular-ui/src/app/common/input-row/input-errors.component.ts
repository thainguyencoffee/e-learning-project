import { KeyValuePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import {getGlobalErrorMessage} from "../error-handler.injectable";


@Component({
  selector: 'app-input-errors',
  standalone: true,
  templateUrl: './input-errors.component.html',
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
