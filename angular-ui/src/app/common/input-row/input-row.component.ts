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
  AbstractControl,
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

  previewUrl: string | null = null;
  previousUrl: string | null = null;
  @ViewChild('videoPlayer') videoPlayer: ElementRef | undefined;

  @Output() optionsChange = new EventEmitter<string>();

  ngOnInit() {
    this.control = this.group!.get(this.field)!;

    if (this.rowType === 'imageFile') {
      this.previewUrl = this.control.value || 'https://placehold.co/400'
    } else {
      this.previewUrl = this.control.value || ''
    }

    this.previousUrl = this.control.value || '' // bất kể rowType là gì thì vẫn đảm bảo previousUrl để clear data khi cần
    this.control.valueChanges.subscribe(value => {
      if (value) {
        this.previewUrl = value
        this.previousUrl = value
      }
    })

  }



  ngOnChanges() {
    if (!this.options || this.options instanceof Map) {
      this.optionsMap = this.options;
    } else {
      this.optionsMap = new Map(Object.entries(this.options));
    }
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
      this.uploadService.deleteAll([this.previousUrl]).subscribe({
        next: () => {
          this.previousUrl = null;
        },
        error: error => this.errorHandler.handleServerError(error, this.group)
      })
    }

  }

}
