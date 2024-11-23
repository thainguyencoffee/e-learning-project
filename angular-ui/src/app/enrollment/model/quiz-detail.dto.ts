export interface AnswerOptionDto {
    id: number;
    content: string;
}

export interface QuestionDto {
    id: number;
    content: string;
    type: string;
    options: AnswerOptionDto[];
    trueFalseAnswer: boolean;
    score: number;
}

export interface QuizDetailDto {
    id: number;
    title: string;
    description: string;
    afterLessonId: number;
    questions: QuestionDto[];
    totalScore: number;
    passScorePercentage: number;
}
