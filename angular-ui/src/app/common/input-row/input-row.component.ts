import { KeyValuePipe, NgForOf, NgIf } from '@angular/common';
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
  FormArray,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { InputErrorsComponent } from "./input-errors.component";
import flatpickr from 'flatpickr';
import {FileRowComponent} from "./shared/file-row.component";

@Component({
  selector: 'app-input-row',
  standalone: true,
  templateUrl: './input-row.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe, FormsModule, NgForOf, NgIf, FileRowComponent],
})
export class InputRowComponent implements AfterViewInit, OnChanges {

  private elRef = inject(ElementRef);

  @Input() group?: FormGroup;
  @Input() field = '';
  @Input() formArray?: FormArray;
  @Input() index?: number;
  @Input() controlFromArray?: FormControl;
  @Input() rowType = 'text';
  @Input() inputClass = '';
  @Input() options?: Record<string, string> | Map<number, string>;
  @Input() label = '';
  @Input() placeholder?: string = '';
  @Input() datepicker?: 'datepicker' | 'timepicker' | 'datetimepicker';
  @Output() optionsChange = new EventEmitter<string>();

  optionsMap?: Map<string | number, string>;
  randomId = Math.random().toString(36).substring(7);

  get dynamicControl(): FormControl | null {
    return this.group?.get(this.field) as FormControl || this.controlFromArray || null;
  }

  ngOnChanges() {
    this.optionsMap = this.options instanceof Map ? this.options : new Map(Object.entries(this.options || {}));
  }

  ngAfterViewInit() {
    this.initDatepicker();
  }

  onRadioOptionsSelected(option: any) {
    this.optionsChange.emit(option);
  }

  @HostListener('input', ['$event.target'])
  onInputEvent(target: HTMLInputElement) {
    if (!target.value) {
      this.dynamicControl?.setValue(null);
    }
  }

  isRequired() {
    return this.dynamicControl?.hasValidator(Validators.required);
  }

  getInputClasses() {
    return `${this.hasErrors() ? 'is-invalid ' : ''}${this.inputClass}`;
  }

  hasErrors() {
    return this.dynamicControl?.invalid && (this.dynamicControl?.dirty || this.dynamicControl?.touched);
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

    this.dynamicControl?.valueChanges.subscribe((val) => {
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
