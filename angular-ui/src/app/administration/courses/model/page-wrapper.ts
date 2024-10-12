interface Page {
  size: number,
  number: number,
  totalElements: number,
  totalPages: number
}

export interface PageWrapper {
  content: any,
  page: Page
}
