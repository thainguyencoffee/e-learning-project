import {Component, inject} from '@angular/core';
import {CourseService} from "../../service/course.service";
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AddCourseDto} from "../../model/add-course.dto";
import {Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {validJson} from "../../../../common/utils";
import {FieldConfiguration} from "../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../common/input-row/array/array-row.component";

@Component({
  selector: 'app-add-course',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent,
    ArrayRowComponent
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

  benefitsFieldConfiguration: FieldConfiguration = {
    type: 'text',
    placeholder: 'Enter benefit'
  }

  prerequisitesFieldConfiguration: FieldConfiguration = {
    type: 'text',
    placeholder: 'Enter prerequisite'
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(10)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    thumbnailUrl: new FormControl(null),
    language: new FormControl(null, [Validators.required]),
    benefits: new FormArray([]),
    prerequisites: new FormArray([]),
    subtitles: new FormControl([])
  });

  get benefits() {
    return this.addForm.get('benefits') as FormArray;
  }

  createBenefit() {
    return new FormControl(null, [Validators.required, Validators.minLength(25), Validators.maxLength(255)])
  }

  addBenefit() {
    this.benefits.push(this.createBenefit());
  }

  removeBenefit(index: number) {
    this.benefits.removeAt(index);
  }

  get prerequisites() {
    return this.addForm.get('prerequisites') as FormArray;
  }

  addPrerequisite() {
    this.prerequisites.push(this.createBenefit())
  }

  removePrerequisite(index: number) {
    this.prerequisites.removeAt(index);
  }

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
