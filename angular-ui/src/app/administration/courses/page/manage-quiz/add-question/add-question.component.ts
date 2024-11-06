import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../../common/input-row/input-row.component";
import {InputObjectRowComponent} from "../../../../../common/input-object-row/input-object-row.component";
import {FieldConfiguration} from "../../../../../common/input-object-row/field-configuration";
import {QuestionDto} from "../../../model/question.dto";

@Component({
  selector: 'app-add-question',
  standalone: true,
  imports: [
    RouterLink,
    InputRowComponent,
    ReactiveFormsModule,
    InputObjectRowComponent
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

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      added: `Question was added successfully.`
    };
    return messages[key];
  }

  questionTypesMap: Record<string, string> = {
    'MULTIPLE_CHOICE': 'Multiple Choice',
    'SINGLE_CHOICE': 'Single Choice',
    'TRUE_FALSE': 'True/False',
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
        'true': 'true',
        'false': 'false'
      }
    }
  ]

  addForm = new FormGroup({
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]),
    type: new FormControl(null, [Validators.required]),
    options: new FormArray([this.createAnswerOption()]),
    score: new FormControl(null, [Validators.required, Validators.min(1), Validators.max(5)])
  })

  get options(): FormArray {
    return this.addForm.get('options') as FormArray;
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
    this.addForm.markAllAsTouched();
    if (this.addForm.invalid) {
      return;
    }
    const data = new QuestionDto(this.addForm.value);

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

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];
    this.quizId = +this.route.snapshot.params['quizId'];

  }

}
