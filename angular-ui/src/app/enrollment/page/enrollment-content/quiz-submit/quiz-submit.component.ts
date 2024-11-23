import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {of, Subscription, switchMap} from "rxjs";
import {QuestionDto, QuizDetailDto} from "../../../model/quiz-detail.dto";
import {EnrollmentsService} from "../../../service/enrollments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {DatePipe, NgForOf, NgIf, NgTemplateOutlet} from "@angular/common";
import {QuizSubmitDto} from "../../../model/quiz-submit.dto";
import {QuizSubmission} from "../../../model/quiz-submission";

@Component({
  selector: 'app-quiz-submit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf,
    DatePipe,
    NgTemplateOutlet,
  ],
  templateUrl: './quiz-submit.component.html',
  styleUrl: './quiz-submit.component.css'
})
export class QuizSubmitComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);
  fb = inject(FormBuilder);

  navigationSubscription?: Subscription;
  quizId?: number;
  enrollmentId?: number;
  quizSubmissionId?: number;
  isSubmitted?: boolean;
  toggleResubmit = false;

  quizDetail?: QuizDetailDto;
  quizSubmission?: QuizSubmission;

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

  submitQuizForm?: FormGroup;

  private loadData() {
    this.route.parent?.params.subscribe(params => {
      this.enrollmentId = params['id'];
    });
    this.quizId = this.route.snapshot.params['quizId'];
    this.quizSubmissionId = this.route.snapshot.queryParams['quizSubmissionId'];

    this.enrollmentService.getQuiz(this.enrollmentId!, this.quizId!)
      .pipe(
        switchMap(quizDetail => {
          // initialize form all the time
          this.quizDetail = quizDetail;
          this.initForm(this.quizDetail!);

          return of(!!this.quizSubmissionId);
        }),
        switchMap(isSubmitted => {
          this.isSubmitted = isSubmitted;
          if (isSubmitted) {
            return this.enrollmentService.getQuizSubmission(this.enrollmentId!, this.quizSubmissionId!);
          } else {
            return []; // when return [] it will not trigger next
          }
        })
      ).subscribe({
      next: result => {
        if (this.isSubmitted) {
          this.quizSubmission = result as QuizSubmission;
          // this.initFormFromSubmission(this.quizSubmission);
        }
      },
      error: error => this.errorHandler.handleServerError(error.error)
    });
  }

  private initForm(quizDetail: QuizDetailDto) {
    this.submitQuizForm = this.fb.group({
      quizId: [quizDetail.id],
      questions: this.fb.array(quizDetail.questions.map(q => {
        return this.fb.group({
          type: [q.type],
          questionId: [q.id],
          answerOptionIds: q.type === 'MULTIPLE_CHOICE' ? this.fb.array(q.options.map(o => {
            return this.fb.control(false);
          })) : [null],
          trueFalseAnswer: [null],
          singleChoiceAnswer: q.type === 'SINGLE_CHOICE' ? [null] : null
        })
      }))
    })
  }

  get questions() {
    return this.submitQuizForm!.get('questions') as FormArray;
  }

  getAnswerOptionIds(i: number) {
    return this.questions.at(i).get('answerOptionIds') as FormArray;
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      submitted: `Quiz was submitted successfully.`
    };
    return messages[key];
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.submitQuizForm!.markAllAsTouched();
    if (!this.submitQuizForm!.valid) {
      return;
    }

    const submission = {
      ...this.submitQuizForm!.value,
      questions: this.submitQuizForm!.value.questions.map((question: any, index: number) => {
        if (question.type === 'MULTIPLE_CHOICE') {
          const selectedIds = question.answerOptionIds
            .map((checked: boolean, optionIndex: number) =>
              checked ? this.quizDetail!.questions[index].options[optionIndex].id : null
            )
            .filter((id: number | null) => id !== null);
          return { ...question, answerOptionIds: selectedIds };
        }
        return question;
      })
    };

    const data = new QuizSubmitDto(submission);

    this.enrollmentService.submitQuiz(this.enrollmentId!, data)
      .subscribe({
        next: submissionId => {
          this.router.navigate(['.'], {
            relativeTo: this.route,
            queryParams: {
              quizSubmissionId: submissionId,
            },
            state: {
              msgSuccess: this.getMessage('submitted')
            }
          });
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  getQuestionById(questionId: number, questions: QuestionDto[]) {
    return questions.find(q => q.id === questionId);
  }

  getAnswerOptionById(answerOptionId: number, question: QuestionDto) {
    return question.options.find(o => o.id === answerOptionId);
  }

  getAnswerOptionsByIds(answerOptionIds: number[], question: QuestionDto) {
    return question.options.filter(o => answerOptionIds.includes(o.id));
  }

}
