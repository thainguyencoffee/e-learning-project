import {Component, inject, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {SectionDto} from "../../model/section-dto";

@Component({
  selector: 'app-add-section',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent,
  ],
  templateUrl: './add-section.component.html',
})
export class AddSectionComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  currentCourseId?: number;
  course?: Course;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Section was added successfully.`
    };
    return messages[key];
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)]),
  })

  ngOnInit(): void {
    this.currentCourseId = +this.route.snapshot.params['id'];
    this.courseService.getCourse(this.currentCourseId)
      .subscribe({
        next: data => this.course = data,
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    const data = new SectionDto(this.addForm.value);
    this.courseService.addSection(this.currentCourseId!, data).subscribe({
      next:() => this.router.navigate(['/administration/courses'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    })
  }

}
