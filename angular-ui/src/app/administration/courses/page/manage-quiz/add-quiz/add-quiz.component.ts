import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../../service/course.service";
import {ErrorHandler} from "../../../../../common/error-handler.injectable";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../../common/input-row/input-row.component";
import {AddCourseDto} from "../../../model/add-course.dto";

@Component({
  selector: 'app-add-quiz',
  standalone: true,
  imports: [
    RouterLink,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './add-quiz.component.html',
})
export class AddQuizComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Quiz was created successfully.`
    };
    return messages[key];
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    afterLessonId: new FormControl<number | null>(null, []),
    passScorePercentage: new FormControl<number | null>(null, [Validators.required, Validators.min(0), Validators.max(100)]),
  })

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      console.log("Invalid form");
      return;
    }

    this.addForm.patchValue({afterLessonId: this.lessonId});

    const data = new AddCourseDto(this.addForm.value);
    this.courseService.addQuizToSection(this.courseId!, this.sectionId!, data).subscribe({
      next: () => this.router
        .navigate(['../'], {
          relativeTo: this.route,
          state: {
            msgSuccess: this.getMessage('created')
          }
        }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    })
  }

}
