import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Salary} from "../model/salary";

@Injectable({
  providedIn: 'root'
})
export class SalaryService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/salaries'

  getSalaryByTeacher(teacher: string) {
    return this.http.get<Salary>(`${this.resourcePath}/${teacher}`);
  }

}
