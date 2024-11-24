export interface LessonMarkRequest {
  mark: MarkType;
  courseId: number;
  lessonId: number;
}

export enum MarkType {
  COMPLETED = "COMPLETED",
  INCOMPLETE = "INCOMPLETE"
}
