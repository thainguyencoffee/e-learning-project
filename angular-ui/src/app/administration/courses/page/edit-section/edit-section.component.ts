import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {updateForm} from "../../../../common/utils";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {SectionDto} from "../../model/section-dto";

@Component({
  selector: 'app-edit-course',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './edit-section.component.html',
})
export class EditSectionComponent implements OnInit{

  courseService = inject(CourseService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler)

  courseId?: number;
  sectionId?: number;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Course was updated successfully.`
    };
    return messages[key];
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.maxLength(255), Validators.minLength(10)]),
  });

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['id'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.courseService.getCourse(this.courseId)
      .subscribe({
        next: (data) => updateForm(this.editForm, data.sections?.find(section => section.id === this.sectionId)),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new SectionDto(this.editForm.value);
    this.courseService.updateSection(this.courseId!, this.sectionId, data)
      .subscribe({
        next: () => this.router.navigate(['/administration/courses', this.courseId], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });
  }

}


