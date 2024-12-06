export interface CourseOrder {
  id: number,
  courseId: number,
  orderIndex: number,
}

export interface CoursePath {
  id: number,
  title: string,
  courseOrders: CourseOrder[],
  description: string,
  teacher: string,
  published: boolean,
  publishedDate?: string,
  createdDate: string,
  createdBy: string,
  lastModifiedDate: string,
  lastModifiedBy: string,
}
