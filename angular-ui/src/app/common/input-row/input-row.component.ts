import { KeyValuePipe } from '@angular/common';
import { Component, HostListener, inject, Input, OnChanges, OnInit } from '@angular/core';
import { AbstractControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {InputErrorsComponent} from "./input-errors.component";


@Component({
  selector: 'app-input-row',
  standalone: true,
  templateUrl: './input-row.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe]
})
export class InputRowComponent implements OnChanges, OnInit {

  @Input({ required: true })
  group?: FormGroup;

  @Input({ required: true })
  field = '';

  @Input()
  rowType = 'text';

  // custom css
  @Input()
  inputClass = '';

  @Input()
  options?: Record<string,string>|Map<number,string>;

  @Input({ required: true })
  label = '';

  control?: AbstractControl;
  optionsMap?: Map<string|number,string>;

  ngOnInit() {
    this.control = this.group!.get(this.field)!;
  }

  ngOnChanges() {
    if (!this.options || this.options instanceof Map) {
      this.optionsMap = this.options;
    } else {
      this.optionsMap = new Map(Object.entries(this.options));
    }
  }

  // thống nhất dữ liệu
  @HostListener('input', ['$event.target'])
  onEvent(target: HTMLInputElement) {
    if (target.value === '') {
      this.control!.setValue(null);
    }
  }

  isRequired() {
    return this.control?.hasValidator(Validators.required);
  }

  getInputClasses() {
    return (this.hasErrors() ? 'is-invalid ' : '') + this.inputClass;
  }

  hasErrors() {
    return this.control?.invalid && (this.control?.dirty || this.control?.touched);
  }

}
