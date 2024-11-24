import {LessonProgress} from "./lesson-progress";
import {Section} from "../../administration/courses/model/view/section";
import {QuizSubmission} from "./quiz-submission";
import {Progress} from "./progress";

export interface Certificate {
  id: number,
  fullName: string,
  email: string,
  student: string,
  teacher: string,
  url: string,
  courseId: number,
  courseTitle: string,
  issuedDate: string,
  certified: boolean
}

export interface EnrollmentWithCourseDto {
  courseId: number,
  title: string,
  thumbnailUrl?: string,
  description?: string,
  language: string,
  subtitles: string[],
  benefits: string[],
  prerequisites: string[],
  sections: Section[],
  teacher: string,
  enrollmentId: number,
  student: string,
  enrollmentDate: string,
  lessonProgresses: LessonProgress[],
  completed: boolean,
  reviewed: boolean,
  completedDate: string,
  certificate: Certificate,
  progress: Progress,
  quizSubmissions: QuizSubmission[]
}
