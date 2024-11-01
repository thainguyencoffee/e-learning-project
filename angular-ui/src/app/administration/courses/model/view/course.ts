import {Section} from "./section";
import {CourseRequest} from "./course-request";

export interface Course {
  id: number;
  title: string;
  thumbnailUrl?: string;
  description?: string;
  language: string,
  subtitles?: string[],
  benefits?: string[],
  prerequisites?: string[],
  sections?: Section[],
  price?: string,
  published: boolean,
  unpublished: boolean,
  teacher: string,
  approvedBy?: string,
  courseRequests?: CourseRequest[],
  students: string[],
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string,
}
