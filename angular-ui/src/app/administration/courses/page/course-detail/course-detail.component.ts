import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NgForOf, NgIf} from "@angular/common";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
  ],
  templateUrl: './course-detail.component.html',
})
export class CourseDetailComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute)
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  currentId?: number;
  courseDto?: Course
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData()

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
    this.currentId = +this.route.snapshot.params['id']
    this.courseService.getCourse(this.currentId)
      .subscribe({
        next: (data) => this.courseDto = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to delete this element?',
      deleted: `Course section was removed successfully.`
    }
    return messages[key];
  }

  confirmDelete(sectionId: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteSection(this.currentId!, sectionId)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses', this.currentId], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }


}
