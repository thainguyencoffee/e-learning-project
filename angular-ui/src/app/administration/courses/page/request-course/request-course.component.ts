import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {CourseRequestDto} from "../../model/course-request.dto";
import {CourseService} from "../../service/course.service";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {UserService} from "../../../../common/auth/user.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";

@Component({
  selector: 'app-request-course',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    InputRowComponent
  ],
  templateUrl: './request-course.component.html',
})
export class RequestCourseComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  courseService = inject(CourseService);
  userService = inject(UserService);

  requestType: string = '';
  courseId?: number;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Request was created successfully.`
    };
    return messages[key];
  }

  requestPublishAddForm = new FormGroup({
    type: new FormControl(this.requestType || null),
    message: new FormControl(null, [Validators.required, Validators.maxLength(2000), Validators.minLength(25)])
  })

  ngOnInit(): void {
    this.requestType = this.route.snapshot.data['requestType'].toUpperCase()
    this.courseId = this.route.snapshot.params['courseId'];

    this.requestPublishAddForm.controls['type'].setValue(this.requestType);
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.requestPublishAddForm.markAllAsTouched();
    if (!this.requestPublishAddForm.valid) {
      return;
    }

    const data = new CourseRequestDto(this.requestPublishAddForm.value);
    data.requestedBy = this.userService.current.name;

    this.courseService.createRequestCourse(this.courseId!, data).subscribe({
      next:() => this.router.navigate(['/administration/courses'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler
        .handleServerError(error.error, this.requestPublishAddForm, this.getMessage)
    })
  }

}
