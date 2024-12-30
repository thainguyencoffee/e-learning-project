import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {TeacherDto} from "../model/teacher.dto";
import {TeacherDetailDto} from "../model/teacher-detail.dto";
import {environment} from "../../../../environments/environment";

@Injectable(
  {providedIn: 'root'}
)
export class TeacherService {

  http = inject(HttpClient);
  resourcePath = environment.apiPath + '/api/teachers'

  getTeachers(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<TeacherDto>>(url)
  }

  getTeacherDetail(teacher: string, year?: number, pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}/${teacher}?year=${year}&page=${pageNumber}&size=${pageSize}`;
    return this.http.get<TeacherDetailDto>(url)
  }

}
