import {Component, inject} from '@angular/core';
import {CourseService} from "../../service/course.service";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AddCourseDto} from "../../model/add-course.dto";
import {Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {validJson} from "../../../../common/utils";

@Component({
  selector: 'app-add-course',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent
  ],
  templateUrl: './add-course.component.html',
})
export class AddCourseComponent {
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Course was created successfully.`
    };
    return messages[key];
  }

  languagesMap: Record<string, string> = {
    VIETNAMESE: 'Vietnamese',
    ENGLISH: 'English',
    FRENCH: 'French',
    SPANISH: 'Spanish'
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(10)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    thumbnailUrl: new FormControl(null),
    language: new FormControl(null, [Validators.required]),
    benefits: new FormControl(null, [validJson]),
    prerequisites: new FormControl(null, [validJson]),
    subtitles: new FormControl([])
  });

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    const data = new AddCourseDto(this.addForm.value);

    this.courseService.createCourse(data).subscribe({
      next:() => this.router.navigate(['/administration/courses'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    })
  }

}
