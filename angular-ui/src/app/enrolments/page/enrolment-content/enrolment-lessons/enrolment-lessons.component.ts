import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {LessonProgress} from "../../../model/lesson-progress";

@Component({
  selector: 'app-enrolment-lessons',
  standalone: true,
  imports: [
    AsyncPipe,
    NgIf,
    NgForOf,
    NgClass,
    RouterLink
  ],
  templateUrl: './enrolment-lessons.component.html',
  styleUrl: './enrolment-lessons.component.css'
})
export class EnrolmentLessonsComponent implements OnInit {

  route = inject(ActivatedRoute);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);

  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.enrolmentId = params['id'];
    });

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  isCompleted(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.find(lp => lp.lessonId === lessonId)?.completed;
  }

}
