import {LessonProgress} from "./lesson-progress";
import {Progress} from "./progress";
import {QuizSubmission} from "./quiz-submission";

export interface Enrollment {
  id: number,
  student: string,
  courseId: number,
  teacher: string,
  totalLessons: number,
  enrollmentDate: string,
  lessonProgresses: LessonProgress[],
  quizSubmissions: QuizSubmission[],
  completed: boolean,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string,
  progress: Progress
}
