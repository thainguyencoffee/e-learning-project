import {UserInfo} from "../../../common/auth/user-info";

export interface CountDataDto {
  teacher: string,
  numberOfCourses: number,
  numberOfStudents: number,
  numberOfCertificates: number,
  numberOfDraftCourses: number
}

export interface TeacherDto {
  info: UserInfo,
  count: CountDataDto
}
