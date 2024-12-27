export interface CourseOrderPublishedDto {
  id: number;
  courseId: number;
  orderIndex: number;
  price: string;
  title: string;
  purchaseCount: number;
}

export interface CoursePathPublishedDto {
  id: number;
  title: string;
  description: string;
  courseOrders: CourseOrderPublishedDto[];
  teacher: string;
  publishedDate: string;
}
