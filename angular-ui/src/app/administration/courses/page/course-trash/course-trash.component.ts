import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Course} from "../../model/view/course";
import {CourseService} from "../../service/course.service";
import {UserService} from "../../../../common/auth/user.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-course-trash',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    RouterLink
  ],
  templateUrl: './course-trash.component.html',
})
export class CourseTrashComponent implements OnInit, OnDestroy {

  constructor(
    private courseService: CourseService,
    private userService: UserService) {
  }

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  coursesInTrash?: Course[];
  size!: number;
  number!: number;
  totalElements!: number;
  totalPages!: number;
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete force this element?',
      confirmRestore: 'Do you really want to restore this element?',
      deleted: 'Course was removed successfully.',
      restored: 'Course was restored successfully.'
    }
    return messages[key];
  }

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
    if (pageNumber >= 0 && pageNumber < this.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    const pageRange = [];
    for (let i = 0; i < this.totalPages; i++) {
      pageRange.push(i);
    }
    return pageRange;
  }

  loadData(pageNumber: number): void {
    this.courseService.getAllCoursesInTrash(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.coursesInTrash = pageWrapper.content as Course[];
          this.size = pageWrapper.page.size;
          this.number = pageWrapper.page.number;
          this.totalElements = pageWrapper.page.totalElements;
          this.totalPages = pageWrapper.page.totalPages;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmDeleteForce(course: Course) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.courseService.deleteCourseForce(course)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses'], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }

  }

  confirmRestore(courseId: number) {
    if (confirm(this.getMessage('confirmRestore'))) {
      this.courseService.restoreCourse(courseId)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses'], {
            state: {
              msgSuccess: this.getMessage('restored')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

}
