import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {NgForOf, NgIf, NgOptimizedImage} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";

@Component({
  selector: 'app-list-course',
  standalone: true,
  imports: [
    RouterLink,
    NgOptimizedImage,
    NgForOf,
    NgIf
  ],
  templateUrl: './list-course.component.html',
})
export class ListCourseComponent implements OnInit, OnDestroy {

  courseService = inject(CourseService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);

  courses?: Course[];
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete this element?',
      deleted: 'Course was removed successfully.',
      confirmPublish: 'Do you really want to publish this course?. ' +
        'Before publishing a course, you need to agree to the system requirements in the terms and conditions. ' +
        'Are you sure you want to publish this course?',
      published: 'Course was published successfully.',
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
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }


  loadData(pageNumber: number): void {
    this.courseService.getAllCourses(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.courses = pageWrapper.content as Course[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmDelete(id: number) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.courseService.deleteCourse(id)
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

  isAdminCourse(teacher: string) {
    return this.userService.current.hasAnyRole('ROLE_admin') && this.userService.current.name === teacher;
  }

  isTitleBlue(course: Course) {
    if (course.sections && course.sections.length > 0) {
      for (const section of course.sections) {
        return section.lessons && section.lessons.length > 0;
      }
    }
    return false;
  }

  isTitleGreen(course: Course) {
    return this.isTitleBlue(course) && course.price;
  }

  isEditable(course: Course) {
    return !course.published || course.unpublished;
  }

}
