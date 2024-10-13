import {SectionDto} from "./section.dto";

export interface CourseDto {
  id: number;
  title: string;
  thumbnailUrl?: string;
  description?: string;
  language: string,
  subtitles?: string[],
  benefits?: string[],
  prerequisites?: string[],
  sections?: SectionDto[],
  price?: string,
  discountedPrice?: string,
  published: boolean,
  teacher: string,
  approvedBy?: string,
  // students: string[],
  discountCode?: string,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string,
}
