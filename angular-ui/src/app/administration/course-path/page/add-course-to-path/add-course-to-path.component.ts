import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {CoursePathService} from "../../service/course-path.service";
import {CourseService} from "../../../courses/service/course.service";
import {Course} from "../../../courses/model/view/course";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {map, Subscription, switchMap} from "rxjs";
import {CourseCardComponent} from "../../../../common/component/course-card/course-card.component";
import {NgForOf, NgIf} from "@angular/common";
import {CourseWithoutSections} from "../../../../browse-course/model/course-without-sections";
import {CoursePath} from "../../model/course-path";

@Component({
  selector: 'app-add-course-to-path',
  standalone: true,
  imports: [
    RouterLink,
    CourseCardComponent,
    NgForOf,
    NgIf
  ],
  templateUrl: './add-course-to-path.component.html',
  styleUrl: './add-course-to-path.component.css'
})
export class AddCourseToPathComponent implements OnInit, OnDestroy {

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  route = inject(ActivatedRoute);
  coursePathService = inject(CoursePathService);
  courseService = inject(CourseService);

  coursePathId?: number;
  courses?: Course[];
  coursePath?: CoursePath;
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  private loadData(pageNumber: number): void {
    this.coursePathId = +this.route.snapshot.params['coursePathId'];

    this.coursePathService.getCoursePath(this.coursePathId!)
      .pipe(switchMap((coursePath) => {
        return this.courseService.getAllCoursesPublished(pageNumber).pipe(
          map((pageWrapper) => ({coursePath, pageWrapper}))
        );
      }))
      .subscribe({
        next: ({coursePath, pageWrapper}) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.courses = pageWrapper.content as Course[];
          this.coursePath = coursePath;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmAdd: 'Do you really want to add this course to the course path?',
      added: 'Course was added successfully.',
      confirmRemove: 'Do you really want to remove this course from the course path?',
      removed: 'Course was removed successfully.'
    }
    return messages[key];
  }

  onAdd(course: CourseWithoutSections) {
    if (confirm(this.getMessage('confirmAdd'))) {
      this.coursePathService.addCourseOrder(this.coursePathId!, course.id)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('added')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        })
    }
  }

  onRemove(course: CourseWithoutSections) {
    if (confirm(this.getMessage('confirmRemove'))) {
      this.coursePathService.removeCourseOrder(this.coursePathId!, course.id)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('removed')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        })
    }
  }

  toCourseWithoutSection(course: Course): CourseWithoutSections {
    return {
      id: course.id,
      title: course.title,
      description: course.description!,
      teacher: course.teacher,
      hasPurchased: false,
      reviews: [],
      averageRating: 0,
      benefits: course.benefits,
      prerequisites: course.prerequisites,
      language: course.language,
      price: course.price!,
      subtitles: course.subtitles,
      thumbnailUrl: course.thumbnailUrl!,
    }
  }

  isCourseAdded(courseId: number) {
    return !!this.coursePath?.courseOrders.some(course => course.courseId === courseId);
  }

}
