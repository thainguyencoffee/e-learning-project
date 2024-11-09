import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import { updateFormAdvanced} from "../../../../../common/utils";
import {QuestionDto} from "../../../model/question.dto";
import {InputRowComponent} from "../../../../../common/input-row/input-row.component";
import {FieldConfiguration} from "../../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../../common/input-row/array/array-row.component";

@Component({
  selector: 'app-edit-question',
  standalone: true,
  imports: [
    InputRowComponent,
    ReactiveFormsModule,
    RouterLink,
    ArrayRowComponent
  ],
  templateUrl: './edit-question.component.html',
})
export class EditQuestionComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;
  quizId?: number;
  questionId?: number;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Question was updated successfully.`
    };
    return messages[key];
  }

  questionTypesMap: Record<string, string> = {
    MULTIPLE_CHOICE: 'Multiple Choice',
    SINGLE_CHOICE: 'Single Choice',
    TRUE_FALSE: 'True/False',
  }

  optionGroupConfiguration: FieldConfiguration[] = [
    {
      name: 'content',
      type: 'textarea',
      label: 'Content',
    },
    {
      name: 'correct',
      type: 'radio',
      label: 'Correct',
      options: {
        true: 'true',
        false: 'false'
      }
    }
  ]

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]),
    type: new FormControl(null, [Validators.required]),
    options: new FormArray([this.createAnswerOption()]),
    score: new FormControl(null, [Validators.required, Validators.min(1), Validators.max(5)])
  })

  get options(): FormArray {
    return this.editForm.get('options') as FormArray;
  }

  createAnswerOption(): FormGroup {
    return new FormGroup({
      content: new FormControl(null, [Validators.required, Validators.minLength(1), Validators.maxLength(1000)]),
      correct: new FormControl(null, [Validators.required])
    })
  }

  addAnswerOption() {
    this.options.push(this.createAnswerOption());
  }

  removeAnswerOption(index: number) {
    this.options.removeAt(index);
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (this.editForm.invalid) {
      return;
    }

    const data = new QuestionDto(this.editForm.value);

    this.courseService.updateQuestionToQuiz(this.courseId!, this.sectionId!, this.quizId!, this.questionId!, data)
      .subscribe({
        next: () => {
          this.router.navigate(['../../../'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('updated')
            }
          })
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];
    this.quizId = +this.route.snapshot.params['quizId'];
    this.questionId = +this.route.snapshot.params['questionId'];

    this.courseService.getQuiz(this.courseId, this.sectionId)
      .subscribe({
        next: data => {
          const question = data.content.find(quiz => quiz.afterLessonId === this.lessonId)
            ?.questions?.find(question => question.id === this.questionId) || undefined;
          updateFormAdvanced(this.editForm, question, this.createAnswerOption);
        }
      })
  }

}
