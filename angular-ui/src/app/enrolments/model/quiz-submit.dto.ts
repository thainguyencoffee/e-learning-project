
export class QuestionSubmitDto {
  type?: string | null;
  questionId?: number | null;
  answerOptionIds?: number[] | null;
  trueFalseAnswer?: boolean | null;
}

export class QuizSubmitDto {
  quizId?: number | null;
  questions: QuestionSubmitDto[] = [];
}
