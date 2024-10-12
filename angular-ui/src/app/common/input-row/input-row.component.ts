import { KeyValuePipe } from '@angular/common';
import {Component, HostListener, inject, Input, OnChanges, OnDestroy, OnInit} from '@angular/core';
import { AbstractControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {InputErrorsComponent} from "./input-errors.component";
import {UploadService} from "../upload/upload.service";
import {ErrorHandler} from "../error-handler.injectable";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {NavigationStart, Router} from "@angular/router";


@Component({
  selector: 'app-input-row',
  standalone: true,
  templateUrl: './input-row.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe]
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
    }
  }

  ngOnChanges() {
    if (!this.options || this.options instanceof Map) {
      this.optionsMap = this.options;
    } else {
      this.optionsMap = new Map(Object.entries(this.options));
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
        console.log(`File ${response.url} uploaded successfully`);
        this.control?.setValue(response.url);
        this.previewImageUrl = response.url;
        this.previousImageUrl = response.url;
      },
      error: (error) => this.errorHandler.handleServerError(error.error, this.group)
    })

  }

  deletePreviousFile() {
    if (this.previousImageUrl) {
      const base64EncodedUrl = btoa(this.previousImageUrl);
      this.uploadService.delete(base64EncodedUrl).subscribe({
        next: () => {
          console.log(`File ${this.previousImageUrl} deleted successfully`);
          this.previousImageUrl = null;
        },
        error: error => this.errorHandler.handleServerError(error, this.group)
      })
    }

  }

}
