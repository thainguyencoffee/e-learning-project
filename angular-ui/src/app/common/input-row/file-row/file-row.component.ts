import {Component, ElementRef, inject, Input, OnInit, ViewChild} from '@angular/core';
import {AbstractControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {UploadService} from "../../upload/upload.service";
import {ErrorHandler} from "../../error-handler.injectable";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-file-row',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule
  ],
  template: `
    @if (rowType === 'imageFile') {
      <input type="file" (change)="onFileSelected($event)" class="form-control {{ getInputClasses() }}"
             accept=".jpg, .jpeg, .png, .webp"/>
      <img [src]="previewUrl" alt="Image preview" class="img-thumbnail mt-2" style="max-height: 200px;"/>
    }

    @else if (rowType === 'videoFile') {
      <input type="file" (change)="onFileSelected($event)" class="form-control {{ getInputClasses() }}"
             accept=".mp4"/>
      <video *ngIf="previewUrl" #videoPlayer id="myVideo" controls width="100%">
        <source [src]="previewUrl" type="video/mp4">
      </video>
    }

    @else if (rowType == 'docFile') {
      <input type="file" (change)="onFileSelected($event)" class="form-control {{ getInputClasses() }}"
             accept=".pdf, .doc, .docx"/>
      <p *ngIf="previewUrl">Your document was uploaded: {{ previewUrl }}</p>
    }

  `,
})
export class FileRowComponent implements OnInit {

  uploadService = inject(UploadService);
  errorHandler = inject(ErrorHandler);

  @Input() getInputClasses: () => string = () => '';
  @Input({required: true}) rowType?: string;
  @Input({required: true}) control?: AbstractControl;
  @Input() group?: FormGroup;

  @ViewChild('videoPlayer') videoPlayer: ElementRef | undefined;
  previewUrl: string | null = null;

  ngOnInit(): void {
    this.loadFilePreview();
    this.initializeControlValueChanges()
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files![0];

    this.deletePreviousFile().then(() => {
      this.uploadService.upload(file).subscribe({
        next: (response) => {
          this.control?.setValue(response.url);
          this.previewUrl = response.url;
          this.saveToLocalStorage(response.url);
          this.videoPlayer?.nativeElement.load();
        },
        error: (error) => this.errorHandler.handleServerError(error.error, this.group),
      });
    });
  }

  private loadFilePreview() {
    const uploadedFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
    const currentFieldUrl = uploadedFiles.find((file: any) => file.field === 'nguyennt101')?.url || null;
    this.previewUrl = currentFieldUrl || (this.rowType === 'imageFile' ? 'https://placehold.co/400' : '');
  }

  private initializeControlValueChanges() {
    this.control?.valueChanges.subscribe((value) => {
      if (value) {
        this.previewUrl = value;
        this.saveToLocalStorage(value);
      }
    });
  }

  private async deletePreviousFile() {
    const uploadedFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
    const currentFieldUrl = uploadedFiles.find((file: any) => file.field === 'nguyennt101')?.url || null;

    if (currentFieldUrl) {
      return new Promise<void>((resolve, reject) => {
        this.uploadService.deleteAll([currentFieldUrl]).subscribe({
          next: () => {
            this.removeFromLocalStorage();
            resolve();
          },
          error: (error) => {
            this.errorHandler.handleServerError(error, this.group);
            reject();
          },
        });
      });
    } else {
      return Promise.resolve();
    }
  }

  private saveToLocalStorage(url: string) {
    if (!/^https?:\/\/[^\s/$.?#].[^\s]*$/.test(url)) return;

    const uploadedFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
    const existingFileIndex = uploadedFiles.findIndex((file: any) => file.field === 'nguyennt101');

    if (existingFileIndex > -1) {
      uploadedFiles[existingFileIndex].url = url;
    } else {
      uploadedFiles.push({ field: 'nguyennt101', url });
    }

    localStorage.setItem('uploadedFiles', JSON.stringify(uploadedFiles));
  }

  private removeFromLocalStorage() {
    const uploadedFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
    const updatedFiles = uploadedFiles.filter((file: any) => file.field !== 'nguyennt101');
    localStorage.setItem('uploadedFiles', JSON.stringify(updatedFiles));
  }

}
