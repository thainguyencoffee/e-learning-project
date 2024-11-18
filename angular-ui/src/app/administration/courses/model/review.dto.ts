export class ReviewDto {
  rating?: number | null;
  comment?: string | null;

  constructor(data: Partial<ReviewDto>) {
    Object.assign(this, data);
  }
}
