export interface QuizAnswer {
  id: number;
  questionId: number;
  type: string;
  answerOptionIds?: number[];
  trueFalseAnswer?: boolean;
  singleChoiceAnswer?: number;
}

export interface QuizSubmission {
    id: number;
    quizId: number;
    answers: QuizAnswer[];
    score: number;
    submittedDate: string;
    lastModifiedDate: string;
    passed: boolean;
}
