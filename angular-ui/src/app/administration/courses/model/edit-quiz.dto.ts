export class EditQuizDto {
  title?: string | null;
  description?: string | null;
  passScorePercentage?: number | null;

  constructor(data: Partial<EditQuizDto>) {
    Object.assign(this, data);
  }

}
