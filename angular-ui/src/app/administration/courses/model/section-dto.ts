export class SectionDto {
  title?: string | null;

  constructor(data: Partial<SectionDto>) {
    Object.assign(this, data);
  }
}
