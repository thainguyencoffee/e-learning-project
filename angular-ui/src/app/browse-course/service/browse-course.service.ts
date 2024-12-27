import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {catchError, map, Observable, of, switchMap} from "rxjs";
import {PageWrapper, PaginationUtils} from "../../common/dto/page-wrapper";
import {CourseWithoutSections} from "../model/course-without-sections";
import {UserService} from "../../common/auth/user.service";
import {EnrollmentsService} from "../../enrollment/service/enrollments.service";


@Injectable(
  {providedIn: 'root'}
)
export class BrowseCourseService {

  http = inject(HttpClient);
  userService = inject(UserService);
  enrollmentService = inject(EnrollmentsService);

  resourcePath = '/bff/api/published-courses'
  paginationUtils?: PaginationUtils;

  getAllPublishedCourses(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<CourseWithoutSections>> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CourseWithoutSections>>(url)
  }

  getAllPublishedCoursesWithPurchaseStatus(data: Observable<PageWrapper<CourseWithoutSections>>): Observable<{ courses: CourseWithoutSections[]; paginationUtils: PaginationUtils }> {
    return data.pipe(
      switchMap((pageWrapper) => {
        const paginationUtils = new PaginationUtils(pageWrapper.page);
        const allCourses = pageWrapper.content as CourseWithoutSections[];

        if (this.userService.current.isAuthenticated) {
          return this.enrollmentService.getPurchasedCourses().pipe(
            map((purchasedCourses) => ({
              courses: this.markListAsPurchased(allCourses, purchasedCourses),
              paginationUtils,
            }))
          );
        } else {
          return of({
            courses: allCourses,
            paginationUtils,
          });
        }
      }),
      catchError((error) => {
        throw error;
      })
    );
  }

  private markListAsPurchased(allCourses: CourseWithoutSections[], purchasedCourses: number[]): CourseWithoutSections[] {
    return allCourses.map(course => ({
      ...course,
      hasPurchased: purchasedCourses.includes(course.id),
    }));
  }

  getPublishedCourse(courseId: number): Observable<CourseWithoutSections> {
    return this.http.get<CourseWithoutSections>(this.resourcePath + '/' + courseId);
  }

  searchPublishedCourses(query: string, pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<CourseWithoutSections>> {
    const url = `${this.resourcePath}/search?query=${query}&page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<CourseWithoutSections>>(url)
  }

}
