export class AddQuizDto {
  title?: string | null;
  description?: string | null;
  afterLessonId?: number | null;
  passScorePercentage?: number | null;

  constructor(data: Partial<AddQuizDto>) {
    Object.assign(this, data);
  }

}
