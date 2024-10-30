import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {ApproveRequestDto} from "../../model/approve-request.dto";
import {UserService} from "../../../../common/auth/user.service";
import {CourseService} from "../../service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {Course} from "../../model/view/course";
import {RejectRequestDto} from "../../model/reject-request.dto";
import {Observable} from "rxjs";
import {AsyncPipe, NgIf} from "@angular/common";
import {EnrolmentsService} from "../../../../enrolments/service/enrolments.service";

@Component({
  selector: 'app-resolve-request',
  standalone: true,
  imports: [
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule,
    RouterLink,
    NgIf,
    AsyncPipe
  ],
  templateUrl: './resolve-request.component.html',
})
export class ResolveRequestComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  userService = inject(UserService);
  courseService = inject(CourseService);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  requestType: string = '';
  resolveType: string = '';
  courseId?: number;
  requestId?: number;
  course?: Course
  enrolmentsCount$!: Observable<number>;

  isReadDocumentationForUnpublishedCourse = false;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      approved: `Request was approved successfully.`,
      rejected: `Request was rejected successfully.`
    };
    return messages[key];
  }

  approveRequestAddForm = new FormGroup({
    approveType: new FormControl(this.requestType || null),
    approveMessage: new FormControl(null, [Validators.required, Validators.maxLength(2000), Validators.minLength(25)])
  })

  rejectRequestAddForm = new FormGroup({
    rejectType: new FormControl(this.requestType || null),
    rejectCause: new FormControl(null, [Validators.required, Validators.maxLength(2000), Validators.minLength(25)])
  })

  handleSubmit() {
    window.scrollTo(0, 0);
    if (this.resolveType === 'Approve') {
      this.approveRequestAddForm.markAllAsTouched();
      if (!this.approveRequestAddForm.valid) {
        return;
      }
      const data = new ApproveRequestDto(this.approveRequestAddForm.value);
      data.approveBy = this.userService.current.name;

      this.courseService.approveRequest(this.courseId!, this.requestId!, data).subscribe({
        next:() => this.router.navigate(['/administration/courses', this.courseId, 'requests'], {
          state: {
            msgSuccess: this.getMessage('approved')
          }
        }),
        error: (error) => this.errorHandler
          .handleServerError(error.error, this.approveRequestAddForm, this.getMessage)
      })

    } else {
      this.rejectRequestAddForm.markAllAsTouched();
      if (!this.rejectRequestAddForm.valid) {
        return;
      }
      const data = new RejectRequestDto(this.rejectRequestAddForm.value);
      data.rejectBy = this.userService.current.name;

      this.courseService.rejectRequest(this.courseId!, this.requestId!, data).subscribe({
        next:() => this.router.navigate(['/administration/courses', this.courseId, 'requests'], {
          state: {
            msgSuccess: this.getMessage('rejected')
          }
        }),
        error: (error) => this.errorHandler
          .handleServerError(error.error, this.rejectRequestAddForm, this.getMessage)
      })

    }

  }

  ngOnInit(): void {
    this.resolveType = this.route.snapshot.data['resolveType']
    this.courseId = this.route.snapshot.params['courseId'];
    this.requestId = this.route.snapshot.params['requestId'];

    this.enrolmentsCount$ = this.enrolmentService.countEnrolmentsByCourseId(this.courseId!);

    this.courseService.getCourse(this.courseId!).subscribe({
      next: (course) => {
        this.course = course;
        if (course.courseRequests) {

          for (const request of course.courseRequests) {
            if (request.id == this.requestId) {
              this.requestType = request.type;
              this.approveRequestAddForm.controls['approveType'].setValue(this.requestType);
              this.rejectRequestAddForm.controls['rejectType'].setValue(this.requestType);
              break;
            }
          }
        }
      },
      error: (error) => this.errorHandler.handleServerError(error.error)
    })

  }

}
