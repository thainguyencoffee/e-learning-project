import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {map, Observable, switchMap} from "rxjs";
import {CourseDto} from "../model/course.dto";
import {AddCourseDto} from "../model/add-course.dto";
import {UploadService} from "../../../common/upload/upload.service";
import {EditCourseDto} from "../model/edit-course.dto";

@Injectable(
  {providedIn: 'root'}
)
export class CourseService {

  http = inject(HttpClient);
  uploadService = inject(UploadService);

  resourcePath = '/bff/api/courses'

  getAllCourses(): Observable<CourseDto[]> {
    return this.http.get<{content: CourseDto[]}>(this.resourcePath)
      .pipe(map(response => response.content))
  }

  getCourse(id: number): Observable<CourseDto> {
    return this.http.get<CourseDto>(this.resourcePath + '/' + id);
  }

  createCourse(data: AddCourseDto): Observable<CourseDto> {
    return this.http.post<CourseDto>(this.resourcePath, data);
  }

  deleteCourse(id: number, thumbnailUrl?: string) {
    if (thumbnailUrl) {
      return this.uploadService.delete(thumbnailUrl).pipe(
        switchMap(() => this.http.delete<void>(this.resourcePath + '/' + id))
      );
    } else {
      return this.http.delete<void>(this.resourcePath + '/' + id);
    }
  }

  updateCourse(id: number, data: EditCourseDto) {
    return this.http.put<CourseDto>(this.resourcePath + '/' + id, data);
  }

}
