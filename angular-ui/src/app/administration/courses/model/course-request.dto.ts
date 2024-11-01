export class CourseRequestDto {
  type?: string | null;
  message?: string | null;
  requestedBy?: string | null;

  constructor(data: Partial<CourseRequestDto>) {
    Object.assign(this, data);
  }

}
