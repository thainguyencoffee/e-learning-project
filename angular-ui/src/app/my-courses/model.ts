export interface CourseEnrollmentDTO {
  id: number,
  student: string,
  courseId: number,
  title: string,
  thumbnailUrl: string,
  teacher: string,
  enrollmentDate: string,
  completed: boolean,
}
