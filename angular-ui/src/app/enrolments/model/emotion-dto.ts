
export class EmotionDto {
  id?: number | null;
  username?: string | null;
  createdDate?: Date | null;

  constructor(data: Partial<EmotionDto>) {
    Object.assign(this, data);
  }
}
