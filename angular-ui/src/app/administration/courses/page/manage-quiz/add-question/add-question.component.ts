import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {validJson} from "../../../../../common/utils";
import {QuestionDto} from "../../../model/question.dto";
import {InputRowComponent} from "../../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-add-question',
  standalone: true,
  imports: [
    RouterLink,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './add-question.component.html',
})
export class AddQuestionComponent implements OnInit{

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

  addForm = new FormGroup({
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]),
    type: new FormControl(null, [Validators.required]),
    options: new FormControl(null, [Validators.required, validJson]),
    score: new FormControl(null, [Validators.required, Validators.min(1), Validators.max(5)])
  })

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (this.addForm.invalid) {
      return;
    }

    const data = new QuestionDto(this.addForm.value);
    console.log(data)
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
