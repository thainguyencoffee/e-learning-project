import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Salary} from "../model/salary";
import {environment} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SalaryService {

  http = inject(HttpClient);
  resourcePath = environment.apiPath + '/api/salaries'

  getSalaryByTeacher(teacher: string) {
    return this.http.get<Salary>(`${this.resourcePath}/${teacher}`);
  }

}
