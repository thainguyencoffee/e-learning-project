import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {CoursePathService} from "../../service/course-path.service";
import {CoursePathInTrashDto} from "../../model/course-path-in-trash.dto";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-course-path-trash',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf
  ],
  templateUrl: './course-path-trash.component.html',
  styleUrl: './course-path-trash.component.css'
})
export class CoursePathTrashComponent implements OnInit, OnDestroy{

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  coursePathService = inject(CoursePathService);

  coursePathsInTrash?: CoursePathInTrashDto[];
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
    this.coursePathService.getTrashedCoursePaths(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.coursePathsInTrash = pageWrapper.content as CoursePathInTrashDto[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete force this element?',
      confirmRestore: 'Do you really want to restore this element?',
      deleted: 'Course was removed successfully.',
      restored: 'Course was restored successfully.'
    }
    return messages[key];
  }

  confirmRestore(coursePathId: number) {
    if (confirm(this.getMessage('confirmRestore'))) {
      this.coursePathService.restoreCoursePath(coursePathId)
        .subscribe({
          next: () => this.router.navigate(['/administration/course-paths'], {
            state: {
              msgSuccess: this.getMessage('restored')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }
  }

  confirmDeleteForce(coursePathId: number) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.coursePathService.deleteForce(coursePathId)
        .subscribe({
          next: () => this.router.navigate(['/administration/course-paths'], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }
  }

}
