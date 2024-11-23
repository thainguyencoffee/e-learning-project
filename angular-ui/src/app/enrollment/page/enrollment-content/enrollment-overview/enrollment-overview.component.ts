import {Component, inject, OnInit} from '@angular/core';
import {EnrollmentWithCourseDataService} from "../enrollment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrollmentWithCourseDto} from "../../../model/enrollment-with-course-dto";
import {ActivatedRoute, Router} from "@angular/router";
import {AsyncPipe, DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {ReviewDto} from "../../../../administration/courses/model/review.dto";
import {CourseService} from "../../../../administration/courses/service/course.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    NgForOf,
    DatePipe,
    ReactiveFormsModule,
    InputRowComponent,
    DecimalPipe
  ],
  templateUrl: './enrollment-overview.component.html',
  styleUrl: './enrollment-overview.component.css'
})
export class EnrollmentOverviewComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrollmentWithCourseDataService = inject(EnrollmentWithCourseDataService);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  enrollmentId?: number;
  enrollmentWithCourse$!: Observable<EnrollmentWithCourseDto | null>;

  toggleReviewBtn: boolean = false;

  ratingOptions: Record<number, string> = {
    1: 'Very Bad 👎',
    2: 'Bad 💤',
    3: 'Average 😁',
    4: 'Good 😍',
    5: 'Excellent 💯'
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      reviewed: `Your review has been submitted successfully.`,
    };
    return messages[key];
  }

  reviewForm: FormGroup = new FormGroup({
    rating: new FormControl(null, [Validators.required, Validators.min(1), Validators.max(5)]),
    comment: new FormControl(null, [Validators.maxLength(500)])
  });

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.enrollmentId = +params['id'];
    });

    this.enrollmentWithCourse$ = this.enrollmentWithCourseDataService.enrollmentWithCourse$;
  }

  onToggleReviewBtn() {
    this.toggleReviewBtn = !this.toggleReviewBtn;
  }

  handleSubmit(courseId: number) {
    window.scrollTo(0, 0);
    this.reviewForm.markAllAsTouched();
    if (!this.reviewForm.valid) {
      return;
    }

    const data = new ReviewDto(this.reviewForm.value);

    this.courseService.submitReview(courseId, this.enrollmentId!, data).subscribe({
      next:() => this.router.navigate(['.'], {
        relativeTo: this.route,
        state: {
          msgSuccess: this.getMessage('reviewed')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.reviewForm, this.getMessage)
    })
  }

}
