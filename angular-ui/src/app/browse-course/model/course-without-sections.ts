export interface CourseWithoutSections {
  id: number,
  title: string,
  thumbnailUrl: string,
  description: string,
  language: string,
  subtitles: string[],
  benefits: string[],
  prerequisites: string[],
  price: string,
  teacher: string
}
