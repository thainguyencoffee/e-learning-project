import {Lesson} from "./lesson";
import {Quiz} from "./quiz";

export interface Section {
  id : number;
  title : string;
  orderIndex : number;
  lessons: Lesson[],
  quizzes: Quiz[]
}

