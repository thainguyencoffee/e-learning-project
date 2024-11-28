import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {updateFormAdvanced} from "../../../../common/utils";
import {FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {EditCourseDto} from "../../model/edit-course.dto";
import {FieldConfiguration} from "../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../common/input-row/array/array-row.component";

@Component({
  selector: 'app-edit-course',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule,
    ArrayRowComponent,
  ],
  templateUrl: './edit-course.component.html',
})
export class EditCourseComponent implements OnInit {

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

  benefitsFieldConfiguration: FieldConfiguration = {
    type: 'text',
    placeholder: 'Enter benefit'
  }

  prerequisitesFieldConfiguration: FieldConfiguration = {
    type: 'text',
    placeholder: 'Enter prerequisite'
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(10)]),
    description: new FormControl(null, [Validators.maxLength(2000)]),
    thumbnailUrl: new FormControl(null),
    benefits: new FormArray([]),
    prerequisites: new FormArray([]),
    subtitles: new FormControl([])
  });

  get benefits() {
    return this.editForm.get('benefits') as FormArray;
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
    return this.editForm.get('prerequisites') as FormArray;
  }

  addPrerequisite() {
    this.prerequisites.push(this.createBenefit())
  }

  removePrerequisite(index: number) {
    this.prerequisites.removeAt(index);
  }

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
        next: (data) => updateFormAdvanced(this.editForm, data, this.createBenefit),
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


