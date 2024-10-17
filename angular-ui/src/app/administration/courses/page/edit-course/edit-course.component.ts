import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {updateForm} from "../../../../common/utils";
import {FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {EditCourseDto} from "../../model/edit-course.dto";

@Component({
  selector: 'app-edit-course',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './edit-course.component.html',
})
export class EditCourseComponent implements OnInit{

  courseService = inject(CourseService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler)

  currentId?: number;

  languagesMap: Record<string, string> = {
    VIETNAMESE: 'Vietnamese',
    ENGLISH: 'English',
    FRENCH: 'French',
    SPANISH: 'Spanish'
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.maxLength(50)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    thumbnailUrl: new FormControl(null),
    benefits: new FormArray([], []),
    prerequisites: new FormArray([], []),
    subtitles: new FormControl([])
  }, { updateOn: 'submit' });

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Course was updated successfully.`
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.currentId = +this.route.snapshot.params['id'];
    this.courseService.getCourse(this.currentId)
      .subscribe({
        next: (data) => updateForm(this.editForm, data),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new EditCourseDto(this.editForm.value);
    this.courseService.updateCourse(this.currentId!, data)
      .subscribe({
        next: () => this.router.navigate(['/administration/courses'], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });
  }

}


