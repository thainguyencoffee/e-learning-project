import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {NgForOf, NgOptimizedImage} from "@angular/common";

@Component({
  selector: 'app-list-course',
  standalone: true,
  imports: [
    RouterLink,
    NgOptimizedImage,
    NgForOf
  ],
  templateUrl: './list-course.component.html',
})
export class ListCourseComponent implements OnInit{

  constructor(
    private courseService: CourseService) {
  }

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  courses?: Course[];
  size!: number;
  number!: number;
  totalElements!: number;
  totalPages!: number;
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to delete this element?',
      deleted: 'Course was removed successfully.'
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
    this.courseService.getAllCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.courses = pageWrapper.content as Course[];
          this.size = pageWrapper.page.size;
          this.number = pageWrapper.page.number;
          this.totalElements = pageWrapper.page.totalElements;
          this.totalPages = pageWrapper.page.totalPages;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmDelete(id: number, thumbnailUrl?: string) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteCourse(id, thumbnailUrl)
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

}
