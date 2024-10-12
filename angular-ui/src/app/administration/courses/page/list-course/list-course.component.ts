import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CourseService} from "../../service/course.service";
import {CourseDto} from "../../model/course.dto";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {NgOptimizedImage} from "@angular/common";

@Component({
  selector: 'app-list-course',
  standalone: true,
  imports: [
    RouterLink,
    NgOptimizedImage
  ],
  templateUrl: './list-course.component.html',
  styleUrl: './list-course.component.css'
})
export class ListCourseComponent implements OnInit, OnDestroy{

  constructor(
    private courseService: CourseService) {
  }

  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  courses?: CourseDto[];
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to delete this element?',
      deleted: 'Course was removed successfully.'
    }
    return messages[key];
  }

  ngOnInit(): void {
    this.loadData();
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  loadData() {
    this.courseService.getAllCourses()
      .subscribe({
        next: (data) => this.courses = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmDelete(id: number, thumbnailUrl?: string) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteCourse(id, thumbnailUrl)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses'], {
            state: {
              msgInfo: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }

  }

}
