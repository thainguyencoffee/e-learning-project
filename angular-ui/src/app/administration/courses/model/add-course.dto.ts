export class AddCourseDto {
  title?: string | null;
  description?: string | null;
  thumbnailUrl?: string | null;
  language?: string | null;
  benefits?: string | null;
  prerequisites?: string | null;
  subtitles?: string[] | null;

  constructor(data: Partial<AddCourseDto>) {
    Object.assign(this, data);
    if (data.benefits) {
      this.benefits = JSON.parse(data.benefits);
    }
    if (data.prerequisites) {
      this.prerequisites = JSON.parse(data.prerequisites);
    }
  }
}
