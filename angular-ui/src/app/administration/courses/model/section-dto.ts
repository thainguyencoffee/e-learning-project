import {LessonDto} from "./lesson-dto";

export interface SectionDto {
  title: string,
  lessons: LessonDto[]
}
