import {LessonDto} from "./lesson.dto";

export interface SectionDto {
  id : number;
  title : string;
  lessons: LessonDto[]
}
