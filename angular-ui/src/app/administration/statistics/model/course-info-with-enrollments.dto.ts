import {Enrollment} from "../../../enrollment/model/enrollment";

export interface CourseInfoWithEnrollmentsDto {
  courseId: number;
  title: string;
  thumbnailUrl: string;
  teacher: string;
  enrollments: Enrollment[];
}
