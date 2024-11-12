import {Enrolment} from "../../../enrolments/model/enrolment";

export interface CourseInfoWithEnrolmentsDto {
  courseId: number;
  title: string;
  thumbnailUrl: string;
  teacher: string;
  enrolments: Enrolment[];
}
