import {Component, inject, OnInit} from '@angular/core';
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrolmentWithCourseDto, Progress} from "../../../model/enrolment-with-course-dto";
import {ActivatedRoute} from "@angular/router";
import {AsyncPipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    NgForOf
  ],
  templateUrl: './enrolment-overview.component.html',
})
export class EnrolmentOverviewComponent implements OnInit{

  route = inject(ActivatedRoute);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);

  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;

  ngOnInit(): void {
    this.enrolmentId = +this.route.snapshot.params['id'];

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  calcProgress(progress: Progress) {
    return Math.round(progress.completedLessons / progress.totalLessons * 100);
  }
}
