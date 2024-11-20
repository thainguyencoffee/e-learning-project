export class QuestionDto {
  content?: string | null;
  type?: string | null;
  options?: { content: string, correct: boolean }[] | null;
  score?: number | null;
  trueFalseAnswer?: boolean | null;

  constructor(data: Partial<QuestionDto>) {
    Object.assign(this, data);
  }

}
