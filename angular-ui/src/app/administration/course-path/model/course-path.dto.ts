export class CoursePathDto {
  title?: string | null;
  description?: string | null

  constructor(data: Partial<CoursePathDto>) {
    Object.assign(this, data);
  }
}
