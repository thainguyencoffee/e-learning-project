export interface CourseInfoWithEnrollmentStatisticDto {
  courseId: number,
  title: string,
  thumbnailUrl: string,
  teacher: string,
  totalEnrollments: number,
  totalCompletedEnrollments: number,
}
