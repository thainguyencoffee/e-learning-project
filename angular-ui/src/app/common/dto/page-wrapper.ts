interface Page {
  size: number,
  number: number,
  totalElements: number,
  totalPages: number
}

export interface PageWrapper<T> {
  content: T[],
  page: Page
}

export class PaginationUtils {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;

  constructor(page: Page) {
    this.size = page.size;
    this.number = page.number;
    this.totalElements = page.totalElements;
    this.totalPages = page.totalPages;
  }

  getPageRange(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

}
