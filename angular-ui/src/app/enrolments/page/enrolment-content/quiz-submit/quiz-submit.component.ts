import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {QuizDetailDto} from "../../../model/quiz-detail.dto";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-quiz-submit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
  ],
  templateUrl: './quiz-submit.component.html',
  styleUrl: './quiz-submit.component.css'
})
export class QuizSubmitComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  navigationSubscription?: Subscription;
  quizId?: number;
  enrolmentId?: number;
  returnUrl?: string;

  quizDetail?: QuizDetailDto;

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

  private loadData() {
    this.route.parent?.params.subscribe(params => {
      this.enrolmentId = params['id'];
    });
    this.quizId = this.route.snapshot.params['quizId'];
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'];

    this.enrolmentService.getQuiz(this.enrolmentId!, this.quizId!)
      .subscribe({
        next: quizDetail => {
          this.quizDetail = quizDetail;
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  protected readonly JSON = JSON;
}
