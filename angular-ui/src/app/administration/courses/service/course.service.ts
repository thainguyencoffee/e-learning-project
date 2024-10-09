import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {CourseDto} from "../model/course.dto";
import {AddCourseDto} from "../model/add-course.dto";

@Injectable(
  {providedIn: 'root'}
)
export class CourseService {

  constructor(private http : HttpClient) {
  }
  resourcePath = '/bff/api/courses'

  getAllCourses(): Observable<CourseDto[]> {
    return this.http.get<{content: CourseDto[]}>(this.resourcePath)
      .pipe(map(response => response.content))
  }

  getProduct(id: number): Observable<CourseDto> {
    return this.http.get<CourseDto>(this.resourcePath + '/' + id);
  }

  createCourse(data: AddCourseDto): Observable<CourseDto> {
    return this.http.post<CourseDto>(this.resourcePath, data);
  }

}
