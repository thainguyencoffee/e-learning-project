import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable, switchMap} from "rxjs";
import {Course} from "../model/view/course";
import {AddCourseDto} from "../model/add-course.dto";
import {UploadService} from "../../../common/upload/upload.service";
import {EditCourseDto} from "../model/edit-course.dto";
import {PageWrapper} from "../model/view/page-wrapper";
import {SectionDto} from "../model/section-dto";

@Injectable(
  {providedIn: 'root'}
)
export class CourseService {

  http = inject(HttpClient);
  uploadService = inject(UploadService);

  resourcePath = '/bff/api/courses'

  getAllCourses(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper>(url)
  }

  getCourse(id: number): Observable<Course> {
    return this.http.get<Course>(this.resourcePath + '/' + id);
  }

  createCourse(data: AddCourseDto): Observable<Course> {
    return this.http.post<Course>(this.resourcePath, data);
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
    return this.http.put<Course>(this.resourcePath + '/' + id, data);
  }

  addSection(id: number, data: SectionDto) {
    return this.http.post(`${this.resourcePath}/${id}/sections`, data)
  }

  deleteSection(currentId: number, sectionId: number) {
    return this.http.delete(`${this.resourcePath}/${currentId}/sections/${sectionId}`)
  }

  updateSection(courseId: number, sectionId: number | undefined, data: SectionDto) {
    return this.http.put<Course>(`${this.resourcePath}/${courseId}/sections/${sectionId}`, data);
  }

}
