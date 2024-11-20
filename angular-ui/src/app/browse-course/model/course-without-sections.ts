import {Review} from "../../administration/courses/model/view/review";

export interface CourseWithoutSections {
  id: number,
  title: string,
  thumbnailUrl: string,
  description: string,
  language: string,
  subtitles?: string[],
  benefits?: string[],
  prerequisites?: string[],
  price: string,
  teacher: string,
  reviews: Review[],
  averageRating: number,
}
