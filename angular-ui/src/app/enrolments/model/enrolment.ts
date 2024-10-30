import {LessonProgress} from "./lesson-progress";

export interface Enrolment {
  id: number,
  student: string,
  courseId: number,
  enrollmentDate: string,
  lessonProgresses: LessonProgress[],
  completed: boolean,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string,
}
