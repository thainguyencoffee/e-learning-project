import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {Quiz} from "../../../model/view/quiz";
import {Subscription} from "rxjs";
import {PaginationUtils} from "../../../../../common/dto/page-wrapper";
import {DatePipe, JsonPipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-quiz-trash',
  standalone: true,
  imports: [
    DatePipe,
    NgForOf,
    NgIf,
    JsonPipe,
    RouterLink
  ],
  templateUrl: './quiz-trash.component.html',
})
export class QuizTrashComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;
  navigationSubscription?: Subscription;
  quizzes?: Quiz[];
  paginationUtils?: PaginationUtils;

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
      if (event instanceof NavigationEnd)
        this.loadData(0);
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

  private loadData(pageNumber: number) {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.courseService.getQuizInTrash(this.courseId, this.sectionId)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.quizzes = pageWrapper.content as Quiz[];
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

  confirmRestore(quizId: number) {
    if (confirm(this.getMessage('confirmRestore'))) {
      this.courseService.restoreQuiz(this.courseId!, this.sectionId!, quizId)
        .subscribe({
          next: () => this.router.navigate(['../'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('restored')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }
  }

  confirmDeleteForce(quizId: number) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.courseService.deleteQuizForce(this.courseId!, this.sectionId!, quizId)
        .subscribe({
          next: () => this.router.navigate(['../'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }
  }
}
