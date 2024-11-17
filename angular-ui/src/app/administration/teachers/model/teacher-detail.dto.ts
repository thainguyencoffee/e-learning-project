import {UserInfo} from "../../../common/auth/user-info";

export interface MonthStats {
  month: number,
  count: number
}

export interface StudentsByCourseDTO {
  id: number,
  title: string,
  thumbnailUrl?: string,
  completedStudents: number,
  totalStudents: number
}

export interface TeacherStatisticsDTO {
  coursesByMonth: MonthStats[],
  draftCoursesByMonth: MonthStats[],
  studentsEnrolledByMonth: MonthStats[],
  studentsByCourse: StudentsByCourseDTO[]
}

export interface TeacherDetailDto {
  info: UserInfo,
  statistics: TeacherStatisticsDTO
}
