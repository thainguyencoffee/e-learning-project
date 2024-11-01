import {Lesson} from "./lesson";

export interface Section {
  id : number;
  title : string;
  lessons: Lesson[]
}
