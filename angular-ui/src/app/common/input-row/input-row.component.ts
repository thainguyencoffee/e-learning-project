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
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {
  FormArray, FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {InputErrorsComponent} from "./input-errors.component";
import {UploadService} from "../upload/upload.service";
import {ErrorHandler} from "../error-handler.injectable";
import {Router} from "@angular/router";
import flatpickr from 'flatpickr';


@Component({
  selector: 'app-input-row',
  standalone: true,
  templateUrl: './input-row.component.html',
  imports: [ReactiveFormsModule, InputErrorsComponent, KeyValuePipe /**/, FormsModule, NgForOf, NgIf]
})
export class InputRowComponent implements AfterViewInit, OnChanges, OnInit {

  uploadService = inject(UploadService)
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  @Input() group?: FormGroup;
  @Input() field = '';

  @Input() formArray?: FormArray;
  @Input() index?: number;
  @Input() controlFromArray?: FormControl;

  @Input()
  rowType = 'text';

  @Input()
  inputClass = '';

  @Input()
  options?: Record<string, string> | Map<number, string>;

  @Input() label = '';
  @Input() placeholder?: string = '';

  @Input()
  datepicker?: 'datepicker'|'timepicker'|'datetimepicker';

  optionsMap?: Map<string | number, string>;

  elRef = inject(ElementRef);

  previewUrl: string | null = null;
  previousUrl: string | null = null;
  @ViewChild('videoPlayer') videoPlayer: ElementRef | undefined;

  @Output() optionsChange = new EventEmitter<string>();

  get dynamicControl(): FormControl | null  {
    if (this.group && this.field) {
      return this.group.get(this.field) as FormControl;
    }
    if (this.formArray && this.controlFromArray) {
      return this.controlFromArray as FormControl;
    }
    return null;
  }

  randomId = Math.random().toString(36).substring(7);

  ngOnInit() {
    if (this.rowType === 'imageFile') {
      this.previewUrl = this.dynamicControl!.value || 'https://placehold.co/400'
    } else {
      this.previewUrl = this.dynamicControl!.value || ''
    }

    this.previousUrl = this.dynamicControl!.value || '' // bất kể rowType là gì thì vẫn đảm bảo previousUrl để clear data khi cần
    this.dynamicControl!.valueChanges.subscribe(value => {
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

  ngAfterViewInit() {
    this.initDatepicker();
  }

  onRadioOptionsSelected(option: any) {
    this.optionsChange.emit(option);
  }

  @HostListener('input', ['$event.target'])
  onEvent(target: HTMLInputElement) {
    if (target.value === '') {
      this.dynamicControl!.setValue(null);
    }
  }

  isRequired() {
    return this.dynamicControl?.hasValidator(Validators.required);
  }

  getInputClasses() {
    return (this.hasErrors() ? 'is-invalid ' : '') + this.inputClass;
  }

  hasErrors() {
    return this.dynamicControl?.invalid && (this.dynamicControl?.dirty || this.dynamicControl?.touched);
  }

  initDatepicker() {
    if (!this.datepicker) {
      return;
    }
    const flatpickrConfig:any = {
      allowInput: true,
      time_24hr: true,
      enableSeconds: true
    };
    if (this.datepicker === 'datepicker') {
      flatpickrConfig.dateFormat = 'Y-m-d';
    } else if (this.datepicker === 'timepicker') {
      flatpickrConfig.enableTime = true;
      flatpickrConfig.noCalendar = true;
      flatpickrConfig.dateFormat = 'H:i:S';
    } else { // datetimepicker
      flatpickrConfig.enableTime = true;
      flatpickrConfig.altInput = true;
      flatpickrConfig.altFormat = 'Y-m-d H:i:S';
      flatpickrConfig.dateFormat = 'Y-m-dTH:i:S';
      // workaround label issue
      flatpickrConfig.onReady = function() {
        const id = this.input.id;
        this.input.id = null;
        this.altInput.id = id;
      };
    }
    const input = this.elRef.nativeElement.querySelector('input');
    const flatpicker = flatpickr(input, flatpickrConfig);
    this.dynamicControl!.valueChanges.subscribe(val => {
      // update in case value changes after initialization
      flatpicker.setDate(val);
    });
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files![0];

    if (this.previousUrl) {
      this.deletePreviousFile();
    }

    this.uploadService.upload(file).subscribe({
      next: response => {
        this.dynamicControl?.setValue(response.url);
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
