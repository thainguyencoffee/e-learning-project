import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NgForOf, NgIf} from "@angular/common";
import {Subscription} from "rxjs";
import {UserService} from "../../../../common/auth/user.service";
import {Section} from "../../model/view/section";
import {Lesson} from "../../model/view/lesson";

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
  userService = inject(UserService);

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
      sectionDeleted: `Course section was removed successfully.`,
      lessonDeleted: `Course lesson was removed successfully.`
    }
    return messages[key];
  }

  confirmDeleteSection(sectionId: number, section: Section) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteSection(this.currentId!, sectionId, section)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses', this.currentId], {
            state: {
              msgSuccess: this.getMessage('sectionDeleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }

  confirmDeleteLesson(sectionId: number, lessonId: number, lesson: Lesson) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteLesson(this.currentId!, sectionId, lessonId, lesson)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses', this.currentId], {
            state: {
              msgSuccess: this.getMessage('lessonDeleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }

  isCreateByYou(teacherId: string) {
    return this.userService.current.name === teacherId;
  }

  isEditable() {
    return !this.courseDto?.published || this.courseDto?.unpublished;
  }

  getIdRequestUnresolved() {
    if (this.courseDto?.courseRequests) {
      return this.courseDto?.courseRequests.find(request => !request.resolved)?.id;
    }
    return null;
  }

}
