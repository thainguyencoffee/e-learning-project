import {SectionDto} from "../../administration/courses/model/section-dto";
import {LessonProgress} from "./lesson-progress";

export interface EnrolmentWithCourseDto {
  courseId: number,
  title: string,
  thumbnailUrl: string,
  description: string,
  language: string,
  subtitles: string[],
  benefits: string[],
  prerequisites: string[],
  sections: SectionDto[],
  teacher: string,
  enrollmentId: number,
  student: string,
  lessonProgresses: LessonProgress[],
  completed: boolean
}
