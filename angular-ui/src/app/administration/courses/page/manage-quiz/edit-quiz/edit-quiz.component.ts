import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {updateForm} from "../../../../../common/utils";
import {InputRowComponent} from "../../../../../common/input-row/input-row.component";
import {EditQuizDto} from "../../../model/edit-quiz.dto";

@Component({
  selector: 'app-edit-quiz',
  standalone: true,
  imports: [
    RouterLink,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './edit-quiz.component.html',
})
export class EditQuizComponent implements OnInit{

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
      updated: `Quiz was updated successfully.`
    };
    return messages[key];
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    passScorePercentage: new FormControl<number | null>(null, [Validators.required, Validators.min(0), Validators.max(100)]),
  })

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new EditQuizDto(this.editForm.value);
    this.courseService.updateQuizInSection(this.courseId!, this.sectionId!, this.quizId!, data)
      .subscribe({
        next: () => {
          this.router.navigate(['../../'], {
            relativeTo: this.route,
            state: {
              msgSuccess: this.getMessage('updated')
            }
          });
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.courseService.getCourse(this.courseId)
      .subscribe({
        next: (data) => {
          const quiz = data.sections?.find(section => section.id === this.sectionId)
            ?.quizzes.find(quiz => quiz.afterLessonId === this.lessonId);
          this.quizId = quiz!.id;
          updateForm(this.editForm, quiz);
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

}
