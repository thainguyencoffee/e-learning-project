import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CoursePath} from "../../model/course-path";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {Subscription} from "rxjs";
import {CoursePathService} from "../../service/course-path.service";
import {NgClass, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-list-course-path',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgClass,
    NgForOf
  ],
  templateUrl: './list-course-path.component.html',
})
export class ListCoursePathComponent implements OnInit, OnDestroy {

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  route = inject(ActivatedRoute);
  coursePathService = inject(CoursePathService);

  coursePaths?: CoursePath[]
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
    this.coursePathService.getCoursePaths(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.coursePaths = pageWrapper.content as CoursePath[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete this element?',
      deleted: 'Course was removed successfully.',
      confirmPublish: 'Do you really want to publish this course path',
      published: 'Course path was published successfully.',
      confirmUnpublish: 'Do you really want to unpublish this course path',
      unpublished: 'Course path was unpublished successfully.'
    }
    return messages[key];
  }

  confirmDelete(coursePathId: number) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.coursePathService.deleteCoursePath(coursePathId)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

  confirmPublish(coursePathId: number) {
    if (confirm(this.getMessage('confirmPublish'))) {
      this.coursePathService.publish(coursePathId)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('published')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

  confirmUnpublish(coursePathId: number) {
    if (confirm(this.getMessage('confirmUnpublish'))) {
      this.coursePathService.unpublish(coursePathId)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('unpublished')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }
}
