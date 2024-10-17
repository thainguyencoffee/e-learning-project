import {KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {
  AbstractControl, FormControl,
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
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe /**/, FormsModule, NgForOf, NgIf]
})
export class InputRowComponent implements OnChanges, OnInit {

  uploadService = inject(UploadService)
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  @Output() optionsMapEvent = new EventEmitter<string>();

  @Input({ required: true })
  group?: FormGroup;

  @Input({ required: true })
  field = '';

  @Input()
  rowType = 'text';

  @Input()
  inputClass = '';

  @Input()
  options?: Record<string, string> | Map<number, string>;

  @Input({ required: true })
  label = '';

  control?: AbstractControl;
  optionsMap?: Map<string | number, string>;

  @Input()
  formArrayName?: string | undefined

  @Input()
  controlFormArray?: FormControl;

  previewUrl: string | null = null;
  previousUrl: string | null = null;
  @ViewChild('videoPlayer') videoPlayer: ElementRef | undefined;

  ngOnInit() {
    this.control = this.group!.get(this.field)!;

    this.initImageFile(this.control);
    this.initVideoFile(this.control);
  }

  initImageFile(control: AbstractControl) {
    if (this.rowType === 'imageFile') {
      this.previewUrl = 'https://placehold.co/400'
      control.valueChanges.subscribe(value => {
        if (value) {
          this.previewUrl = value
          this.previousUrl = value
        } else {
          this.previewUrl = 'https://placehold.co/400'
        }
      })
    }
  }

  initVideoFile(control: AbstractControl) {
    if (this.rowType === 'videoFile') {
      this.previewUrl = ''
      control.valueChanges.subscribe(value => {
        if (value) {
          this.previewUrl = value
          this.previousUrl = value
        } else {
          this.previewUrl = ''
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

    if (this.previousUrl) {
      this.deletePreviousFile();
    }

    this.uploadService.upload(file).subscribe({
      next: response => {
        this.control?.setValue(response.url);
        this.previewUrl = response.url;
        this.previousUrl = response.url;
        // reload native element
        this.videoPlayer?.nativeElement.load();
      },
      error: (error) => this.errorHandler.handleServerError(error.error, this.group)
    })

  }

  deletePreviousFile() {
    if (this.previousUrl) {
      this.uploadService.delete(this.previousUrl).subscribe({
        next: () => {
          this.previousUrl = null;
        },
        error: error => this.errorHandler.handleServerError(error, this.group)
      })
    }

  }

}
