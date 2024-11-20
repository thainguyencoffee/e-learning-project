import {LessonProgress} from "./lesson-progress";
import {ProgressDto} from "./progress.dto";

export interface QuizAnswer {
  id: number,
  questionId: number,
  answerOptionIds: number[],
  trueFalseAnswer: boolean,
  singleChoiceAnswer: number,
  type: string
}

export interface QuizSubmission {
  id: number,
  quizId: number,
  answers: QuizAnswer[],
  score: number,
  submittedDate: string,
  lastModifiedDate: string,
  passed: boolean
}

export interface Enrolment {
  id: number,
  student: string,
  courseId: number,
  enrollmentDate: string,
  lessonProgresses: LessonProgress[],
  quizSubmissions: QuizSubmission[],
  completed: boolean,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string,
  progress: ProgressDto
}
