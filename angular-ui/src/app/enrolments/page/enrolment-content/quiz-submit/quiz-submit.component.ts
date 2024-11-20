import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {map, Subscription, switchMap} from "rxjs";
import {QuizDetailDto} from "../../../model/quiz-detail.dto";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {AddCourseDto} from "../../../../administration/courses/model/add-course.dto";
import {QuizSubmitDto} from "../../../model/quiz-submit.dto";
import {QuizSubmission} from "../../../model/quiz-submission";

@Component({
  selector: 'app-quiz-submit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf,
    RouterLink,
  ],
  templateUrl: './quiz-submit.component.html',
  styleUrl: './quiz-submit.component.css'
})
export class QuizSubmitComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);
  fb = inject(FormBuilder);

  navigationSubscription?: Subscription;
  quizId?: number;
  enrolmentId?: number;
  returnUrl?: string;
  isSubmitted?: boolean;

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

  submitQuizForm?: FormGroup;

  private loadData() {
    this.route.parent?.params.subscribe(params => {
      this.enrolmentId = params['id'];
    });
    this.quizId = this.route.snapshot.params['quizId'];
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'];

    this.enrolmentService.isSubmittedQuiz(this.enrolmentId!, this.quizId!)
      .pipe(
        switchMap(isSubmitted => {
          this.isSubmitted = isSubmitted;
          if (isSubmitted) {
            return this.enrolmentService.getQuizSubmission(this.enrolmentId!, this.quizId!);
          } else {
            return this.enrolmentService.getQuiz(this.enrolmentId!, this.quizId!).pipe(
              map(quizDetail => ({ quizDetail }))
            );
          }
        })
      ).subscribe({
        next: result => {
          if (this.isSubmitted) {
            const quizSubmission = result as QuizSubmission;
            this.initFormFromSubmission(quizSubmission);
          } else {
            const { quizDetail } = result as { quizDetail: QuizDetailDto };
            this.quizDetail = quizDetail;
            this.initForm(quizDetail);
          }
        },
        error: error => this.errorHandler.handleServerError(error.error)
      });
  }

  private initFormFromSubmission(quizSubmission: QuizSubmission) {
    this.submitQuizForm = this.fb.group({
      quizId: [quizSubmission.quizId],
      questions: this.fb.array(quizSubmission.answers.map(answer => {
        return this.fb.group({
          type: [answer.type],
          questionId: [answer.questionId],
          answerOptionIds: answer.type === 'MULTIPLE_CHOICE' ?
            this.fb.array(answer.answerOptionIds?.map(id => this.fb.control(id)) || []) :
            [null],
          trueFalseAnswer: [answer.trueFalseAnswer ?? null],
          singleChoiceAnswer: [answer.singleChoiceAnswer ?? null]
        });
      }))
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

    const data = new QuizSubmitDto(this.submitQuizForm!.value);
    this.enrolmentService.submitQuiz(this.enrolmentId!, data)
      .subscribe({
        next: quizId => {
          if (this.returnUrl) {
            this.router.navigateByUrl(this.returnUrl);
          } else {
            this.router.navigate(['.'], {
              relativeTo: this.route,
              state: {
                msgSuccess: this.getMessage('submitted')
              }
            });
          }
        }
      })
  }

}
