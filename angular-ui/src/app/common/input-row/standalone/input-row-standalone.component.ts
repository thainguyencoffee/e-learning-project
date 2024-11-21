import {KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  OnChanges,
  Output,
} from '@angular/core';
import {
  FormControl,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {ErrorHandler} from "../../error-handler.injectable";
import {Router} from "@angular/router";
import flatpickr from 'flatpickr';
import {InputErrorsComponent} from "../input-errors.component";
import {FileRowComponent} from "../shared/file-row.component";


@Component({
  selector: 'app-input-row-standalone',
  standalone: true,
  templateUrl: './input-row-standalone.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe /**/, FormsModule, NgForOf, NgIf, FileRowComponent]
})
export class InputRowStandaloneComponent implements AfterViewInit, OnChanges {

  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  @Input({required: true}) control?: FormControl;

  @Input() rowType = 'text';

  @Input() index?: number;

  @Input() inputClass = '';

  @Input() options?: Record<string, string> | Map<number, string>;

  @Input() placeholder?: string = '';

  @Input()
  datepicker?: 'datepicker'|'timepicker'|'datetimepicker';

  optionsMap?: Map<string | number, string>;

  elRef = inject(ElementRef);

  @Output() optionsChange = new EventEmitter<string>();

  ngOnChanges() {
    if (!this.options || this.options instanceof Map) {
      this.optionsMap = this.options;
    } else {
      this.optionsMap = new Map(Object.entries(this.options));
    }
  }

  ngAfterViewInit() {
    this.initDatepicker();
  }

  onRadioOptionsSelected(option: any) {
    this.optionsChange.emit(option);
  }

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

  private initDatepicker() {
    if (!this.datepicker) return;

    const config = {
      allowInput: true,
      time_24hr: true,
      enableSeconds: true,
      dateFormat: this.getDateFormat(),
      enableTime: this.datepicker !== 'datepicker',
      noCalendar: this.datepicker === 'timepicker',
    };

    const input = this.elRef.nativeElement.querySelector('input');
    const picker = flatpickr(input, config);

    this.control?.valueChanges.subscribe((val) => {
      picker.setDate(val);
    });
  }

  private getDateFormat() {
    return this.datepicker === 'datepicker'
      ? 'Y-m-d'
      : this.datepicker === 'timepicker'
        ? 'H:i:S'
        : 'Y-m-dTH:i:S';
  }

}
