import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {CoursePathService} from "../../service/course-path.service";
import {updateForm} from "../../../../common/utils";
import {CoursePathDto} from "../../model/course-path.dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-edit-course-path',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './edit-course-path.component.html',
})
export class EditCoursePathComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  coursePathService = inject(CoursePathService);

  coursePathId?: number;

  editForm =  new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(10)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
  })

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Course was updated successfully.`
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.coursePathId = this.route.snapshot.params['coursePathId'];
    this.coursePathService.getCoursePath(this.coursePathId!)
      .subscribe({
        next: data => updateForm(this.editForm, data),
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new CoursePathDto(this.editForm.value);

    this.coursePathService.updateCoursePath(this.coursePathId!, data).subscribe({
      next:() => this.router.navigate(['/administration/course-paths'], {
        state: {
          msgSuccess: this.getMessage('updated')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
    })

  }


}
