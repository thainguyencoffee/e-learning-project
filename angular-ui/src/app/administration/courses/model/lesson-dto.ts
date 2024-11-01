export class LessonDto {
  title?: string | null;
  type?: string | null;
  link?: string | null;

  constructor(data: Partial<LessonDto>) {
    Object.assign(this, data);
  }

}
