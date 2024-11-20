import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {CourseService} from "../../service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {JsonPipe, NgForOf, NgIf} from "@angular/common";
import {AnswerOption, Quiz} from "../../model/view/quiz";

@Component({
  selector: 'app-manage-quiz',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgForOf,
  ],
  templateUrl: './manage-quiz.component.html',
})
export class ManageQuizComponent implements OnInit, OnDestroy{

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;
  quizDto?: Quiz
  navigationSubscription?: Subscription;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDeleteQuestion: 'Do you really want to delete this question?',
      confirmDeleteQuiz: 'Do you really want to delete this quiz?. All questions will be deleted as well.',
      questionDeleted: 'Question was removed successfully.',
      quizDeleted: 'Quiz was removed successfully.'
    }
    return messages[key];
  }

  ngOnInit(): void {
    this.loadData();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd)
        this.loadData();
    })
  }

  loadData() {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.courseService.getQuiz(this.courseId, this.sectionId)
      .subscribe({
        next: data => {
          this.quizDto = data.content.find(quiz => quiz.afterLessonId === this.lessonId);
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  confirmDeleteQuiz(quizId: number) {
    if (confirm(this.getMessage('confirmDeleteQuiz'))) {
      this.courseService.deleteQuiz(this.courseId!, this.sectionId!, quizId)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('quizDeleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

  confirmDeleteQuestion(quizId: number, questionId: number) {
    if (confirm(this.getMessage('confirmDeleteQuestion'))) {
      this.courseService.deleteQuestion(this.courseId!, this.sectionId!, quizId, questionId)
        .subscribe({
          next: () => this.router.navigate(['.'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('questionDeleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

  getCorrectOption(options: AnswerOption[]) {
    return options.filter(option => option.correct);
  }
}
