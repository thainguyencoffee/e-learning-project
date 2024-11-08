import {LessonProgress} from "./lesson-progress";
import {Section} from "../../administration/courses/model/view/section";
import {Post} from "../../administration/courses/model/view/post";

export interface Progress {
  totalLessons: number,
  completedLessons: number,
}

export interface EnrolmentWithCourseDto {
  courseId: number,
  title: string,
  thumbnailUrl: string,
  description: string,
  language: string,
  subtitles: string[],
  benefits: string[],
  prerequisites: string[],
  posts:Post[],
  sections: Section[],
  teacher: string,
  enrollmentId: number,
  student: string,
  lessonProgresses: LessonProgress[],
  completed: boolean,
  progress: Progress
}
