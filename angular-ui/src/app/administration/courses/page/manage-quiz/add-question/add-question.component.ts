import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {AbstractControl, FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {QuestionDto} from "../../../model/question.dto";
import {minFormArrayLength} from "../../../../../common/utils";
import {KeyValuePipe, NgForOf, NgIf} from "@angular/common";
import {onChangeQuestionType} from "../question-utils";
import {InputErrorsComponent} from "../../../../../common/input-row/error/input-errors.component";

@Component({
  selector: 'app-add-question',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    KeyValuePipe,
    NgForOf,
    NgIf,
    InputErrorsComponent
  ],
  templateUrl: './add-question.component.html',
})
export class AddQuestionComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;
  quizId?: number;

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];
    this.quizId = +this.route.snapshot.params['quizId'];

  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      added: `Question was added successfully.`
    };
    return messages[key];
  }

  typeOptions: Record<string, string> = {
    MULTIPLE_CHOICE: 'Multiple Choice',
    SINGLE_CHOICE: 'Single Choice',
    TRUE_FALSE: 'True/False',
  }

  questionForm = new FormGroup({
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]),
    type: new FormControl(null, [Validators.required]),
    score: new FormControl<number|null>(null, [Validators.required, Validators.min(1), Validators.max(5)]),
    options: new FormArray([], [minFormArrayLength(2)]),
    trueFalseAnswer: new FormControl<boolean | null>(null),
  })

  get content() { return this.questionForm.get('content') as FormControl; }
  get type() { return this.questionForm.get('type') as FormControl; }
  get score() { return this.questionForm.get('score') as FormControl;}
  get options() { return this.questionForm.get('options') as FormArray; }
  getContentControl(option: AbstractControl<any>) {
    return option.get('content') as FormControl;
  }

  addAnswerOption() {
    this.options.push(new FormGroup({
      content: new FormControl(null, [Validators.required, Validators.minLength(1), Validators.maxLength(1000)]),
      correct: new FormControl(false, [Validators.required])
    }));
  }

  removeAnswerOption(index: number) {
    this.options.removeAt(index);
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.questionForm.markAllAsTouched();
    if (this.questionForm.invalid) {
      return;
    }
    const data = new QuestionDto(this.questionForm.value);

    this.courseService.addQuestionToQuiz(this.courseId!, this.sectionId!, this.quizId!, data)
      .subscribe({
        next: () => {
          this.router.navigate(['../../'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('added')
            }
          })
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  isMultipleChoice() {
    const type = this.questionForm.get('type') as FormControl;
    return type.value === 'MULTIPLE_CHOICE';
  }

  isSingleChoice() {
    const type = this.questionForm.get('type') as FormControl;
    return type.value === 'SINGLE_CHOICE';
  }

  isTrueFalse() {
    const type = this.questionForm.get('type') as FormControl;
    return type.value === 'TRUE_FALSE';
  }

  handleSingleChoice(i: number) {
    const optionsArray = this.options;
    optionsArray.controls.forEach((control, index) => {
      if (index !== i) {
        control.get('correct')?.setValue(false, { emitEvent: false });
      } else {
        control.get('correct')?.setValue(true, { emitEvent: false });
      }
    });
  }

  onChangeType() {
    const type = this.questionForm.get('type') as FormControl;

    onChangeQuestionType(type, this.questionForm.get('trueFalseAnswer') as FormControl, this.options);
    if (type.value === 'SINGLE_CHOICE') {
      if (this.options.length > 0) {
        this.handleSingleChoice(0);
      }
    }
  }

  isRequired(field: string) {
    return this.questionForm.get(field)?.hasValidator(Validators.required);
  }

  getInputClasses(field: string) {
    return `${this.hasErrors(field) ? 'is-invalid ' : ''}`;
  }

  hasErrors(field: string) {
    return this.questionForm.get(field)?.invalid && (this.questionForm.get(field)?.dirty || this.questionForm.get(field)?.touched);
  }

}
