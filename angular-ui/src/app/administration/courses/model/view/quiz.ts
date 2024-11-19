export interface AnswerOption {
  id: number;
  content: string;
  correct: boolean;
}

export interface Question {
  id: number;
  content: string;
  type: string;
  score: number;
  options?: AnswerOption[];
  trueFalseAnswer?: boolean;
}

export interface Quiz {
  id: number;
  title: string;
  description: string;
  afterLessonId: number;
  questions: Question[];
  totalScore: number;
  passScorePercentage: number;
  deleted: boolean;
}
