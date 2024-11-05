interface AnswerOption {
  id: number;
  content: string;
  correct: boolean;
}

interface Question {
  id: number;
  content: string;
  type: string;
  options: AnswerOption[];
  score: number;
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
