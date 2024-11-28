import {AfterViewInit, Component, ElementRef, EventEmitter, inject, Input, Output, ViewChild} from '@angular/core';
import Quill from "quill";
import {UploadService} from "../upload/upload.service";
import {ErrorHandler} from "../error-handler.injectable";
import {FormControl} from "@angular/forms";
import {filter, first} from "rxjs";

@Component({
  selector: 'app-text-editor',
  standalone: true,
  imports: [],
  template: `
    <div #editorContainer style="height: 400px;"></div>
  `,
})
export class TextEditorComponent implements AfterViewInit{

  @ViewChild('editorContainer', { static: false }) editorContainer!: ElementRef;
  @Output() contentChange = new EventEmitter<string>();
  @Input({required: true}) control?: FormControl;

  uploadService = inject(UploadService);
  errorHandler = inject(ErrorHandler);
  editor!: Quill;

  ngAfterViewInit(): void {
    this.editor = new Quill(this.editorContainer.nativeElement, {
      theme: 'snow',
      modules: {
        toolbar: [
          ['bold', 'italic', 'underline'],
          [{ 'header': [1, 2, false] }],
          [{ 'list': 'ordered' }, { 'list': 'bullet' }],
          ['link', 'image'],
        ]
      }
    })

    this.control?.valueChanges.pipe(
      filter(value => !!value),
      first()
    ).subscribe(value => {
      this.editor.root.innerHTML = value;
    })

    const toolbar = this.editor.getModule('toolbar') as any;
    toolbar.addHandler('image', this.imageHandler.bind(this))

    this.editor.on('text-change', () => {
      this.contentChange.emit(this.editor.root.innerHTML)
    })
  }

  imageHandler() {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.onchange = () => {
      const file = input.files ? input.files[0] : null;
      if (file) {
        this.uploadService.upload(file).subscribe({
          next: objectUrl => {
            const range = this.editor.getSelection(true);
            this.editor.insertEmbed(range.index, 'image', objectUrl.url);
          },
          error: error => this.errorHandler.handleServerError(error.error)
        })
      }
    }
  }

}
