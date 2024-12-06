import {Component, inject} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {CoursePathDto} from "../../model/course-path.dto";
import {CoursePathService} from "../../service/course-path.service";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-add-course-path',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent
  ],
  templateUrl: './add-course-path.component.html',
})
export class AddCoursePathComponent {

  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  coursePathService = inject(CoursePathService);

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Course path was created successfully.`
    };
    return messages[key];
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(20)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
  })

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    const data = new CoursePathDto(this.addForm.value);

    this.coursePathService.createCoursePath(data).subscribe({
      next:() => this.router.navigate(['/administration/course-paths'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    })

  }

}
