import {Component, inject, OnInit} from '@angular/core';
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrolmentWithCourseDto, Progress} from "../../../model/enrolment-with-course-dto";
import {ActivatedRoute, Router} from "@angular/router";
import {AsyncPipe, DatePipe, NgForOf, NgIf} from "@angular/common";
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
    InputRowComponent
  ],
  templateUrl: './enrolment-overview.component.html',
})
export class EnrolmentOverviewComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;

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
      this.enrolmentId = +params['id'];
    });

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  calcProgress(progress: Progress) {
    return Math.round(progress.completedLessons / progress.totalLessons * 100);
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

    this.courseService.submitReview(courseId, this.enrolmentId!, data).subscribe({
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
