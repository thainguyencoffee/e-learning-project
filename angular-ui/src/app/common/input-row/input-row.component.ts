import {KeyValuePipe, NgForOf} from '@angular/common';
import {Component, HostListener, inject, Input, OnChanges, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {InputErrorsComponent} from "./input-errors.component";
import {UploadService} from "../upload/upload.service";
import {ErrorHandler} from "../error-handler.injectable";
import {Router} from "@angular/router";


@Component({
  selector: 'app-input-row',
  standalone: true,
  templateUrl: './input-row.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe, FormsModule, NgForOf]
})
export class InputRowComponent implements OnChanges, OnInit{

  uploadService = inject(UploadService)
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

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
  previewImageUrl: string | null = null;
  previousImageUrl: string | null = null;
  optionsMap?: Map<string|number,string>;

  ngOnInit() {
    this.control = this.group!.get(this.field)!;
    if (this.rowType === 'imageFile') {
      this.previewImageUrl = 'https://placehold.co/400'
      this.control.valueChanges.subscribe(value => {
        if (value) {
          this.previewImageUrl = value
          this.previousImageUrl = value
        } else {
          this.previewImageUrl = 'https://placehold.co/400'
        }
      })
    }
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

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files![0];

    if (this.previousImageUrl) {
      this.deletePreviousFile();
    }

    this.uploadService.upload(file).subscribe({
      next: response => {
        this.control?.setValue(response.url);
        this.previewImageUrl = response.url;
        this.previousImageUrl = response.url;
      },
      error: (error) => this.errorHandler.handleServerError(error.error, this.group)
    })

  }

  deletePreviousFile() {
    if (this.previousImageUrl) {
      this.uploadService.delete(this.previousImageUrl).subscribe({
        next: () => {
          this.previousImageUrl = null;
        },
        error: error => this.errorHandler.handleServerError(error, this.group)
      })
    }

  }

  get formArray(): FormArray {
    return this.group?.get(this.field) as FormArray;
  }

  addItem() {
    this.formArray.push(new FormControl(''))
  }

  removeItem(i: number) {
    this.formArray.removeAt(i);
  }

}
