export class EditCourseDto {
  title?: string | null;
  description?: string | null;
  thumbnailUrl?: string | null;
  benefits?: string[] | null;
  prerequisites?: string[] | null;
  subtitles?: string[] | null;

  constructor(data: Partial<EditCourseDto>) {
    Object.assign(this, data);
  }
}
