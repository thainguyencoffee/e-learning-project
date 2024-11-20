import { Component, Input, OnChanges, SimpleChanges, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import * as mammoth from 'mammoth';

@Component({
  selector: 'app-document-viewer',
  template: `
    <div #documentContainer class="document-container"></div>
  `,
  standalone: true,
  styles: [`
    .document-container {
      width: 100%;
      height: 600px;
      overflow: auto;
      border: 1px solid #ccc;
    }
  `]
})
export class DocumentViewerComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('documentContainer', { static: false }) documentContainer!: ElementRef;
  @Input() documentLink!: string;  // Liên kết tệp DOCX cần hiển thị

  private currentDocument: any;

  ngAfterViewInit(): void {
    this.loadDocument();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['documentLink'] && !changes['documentLink'].firstChange) {
      this.loadDocument();
    }
  }

  private loadDocument(): void {
    if (this.currentDocument) {
      this.clearDocument();
    }

    this.renderDOCX();
  }

  private renderDOCX(): void {
    const container = this.documentContainer.nativeElement;

    // Fetch the DOCX file and render it using mammoth.js
    fetch(this.documentLink)
      .then(response => response.arrayBuffer())
      .then(buffer => {
        mammoth.convertToHtml({ arrayBuffer: buffer })
          .then((result) => {
            container.innerHTML = result.value;
            this.currentDocument = result.value;
          })
          .catch((error) => console.error('Error rendering DOCX:', error));
      })
      .catch((error) => console.error('Error loading DOCX file:', error));
  }

  private clearDocument(): void {
    const container = this.documentContainer.nativeElement;
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
  }

  ngOnDestroy(): void {
    this.clearDocument();
  }
}
