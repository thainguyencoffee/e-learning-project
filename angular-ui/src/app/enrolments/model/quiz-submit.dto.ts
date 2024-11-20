export class QuestionSubmitDto {
  type?: string | null;
  questionId?: number | null;
  answerOptionIds?: number[] | null;
  trueFalseAnswer?: boolean | null;
  singleChoiceAnswer?: number | null;

  constructor(data: Partial<QuestionSubmitDto>) {
    Object.assign(this, data);
  }

}

export class QuizSubmitDto {
  quizId?: number | null;
  questions: QuestionSubmitDto[] = [];

  constructor(data: Partial<QuizSubmitDto>) {
    Object.assign(this, data);
  }

}
