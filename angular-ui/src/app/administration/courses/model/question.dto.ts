export class QuestionDto {
  content?: string | null;
  type?: string | null;
  options?: string | null;
  score?: number | null;

  constructor(data: Partial<QuestionDto>) {
    Object.assign(this, data);
    if (data.options) {
      this.options = JSON.parse(data.options);
    }
  }

}
