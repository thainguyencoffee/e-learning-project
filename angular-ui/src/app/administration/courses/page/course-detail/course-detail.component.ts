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
  styleUrl: './course-detail.component.css'
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
      deleted: 'Course was removed successfully.'
    }
    return messages[key];
  }

  confirmDelete(sectionId: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteSection(this.currentId!, sectionId)
        .subscribe({
          next: () => {
            this.loadData(); // Gọi lại loadData để cập nhật danh sách
            alert(this.getMessage('deleted')); // Hiển thị thông báo thành công
          },
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }


}
