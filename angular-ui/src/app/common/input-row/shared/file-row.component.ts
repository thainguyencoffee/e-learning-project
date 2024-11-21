import {Component, inject, Input, OnInit} from '@angular/core';
import {AbstractControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {UploadService} from "../../upload/upload.service";
import {ErrorHandler} from "../../error-handler.injectable";
import {NgIf} from "@angular/common";
import {VideoPlayerComponent} from "../../video-player/video-player.component";
import {DocumentViewerComponent} from "../../document-viewer/document-viewer.component";

@Component({
  selector: 'app-file-row',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    VideoPlayerComponent,
    DocumentViewerComponent
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
      <app-video-player *ngIf="previewUrl" [videoLink]="previewUrl"></app-video-player>
<!--      <video *ngIf="previewUrl" #videoPlayer id="myVideo" controls width="100%">-->
<!--        <source [src]="previewUrl" type="video/mp4">-->
<!--      </video>-->
    }

    @else if (rowType === 'docFile') {
      <input type="file" (change)="onFileSelected($event)" class="form-control {{ getInputClasses() }}"
             accept=".pdf, .doc, .docx"/>
<!--      <p *ngIf="previewUrl">Your document was uploaded: {{ previewUrl }}</p>-->
      <app-document-viewer *ngIf="previewUrl" [documentLink]="previewUrl"></app-document-viewer>
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

  // @ViewChild('videoPlayer') videoPlayer: ElementRef | undefined;
  previewUrl: string | null = null;
  previousUrl: string | null = null;

  ngOnInit(): void {
    this.initializeControlValueChanges()
  }


  private initializeControlValueChanges() {
    if (this.control) {
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
        // this.videoPlayer?.nativeElement.load();
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
