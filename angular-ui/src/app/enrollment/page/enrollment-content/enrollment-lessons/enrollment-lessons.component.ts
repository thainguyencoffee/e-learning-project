import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {EnrollmentWithCourseDataService} from "../enrollment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrollmentWithCourseDto} from "../../../model/enrollment-with-course-dto";
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {LessonProgress} from "../../../model/lesson-progress";

@Component({
  selector: 'app-enrollment-lessons',
  standalone: true,
  imports: [
    AsyncPipe,
    NgIf,
    NgForOf,
    NgClass,
    RouterLink
  ],
  templateUrl: './enrollment-lessons.component.html',
})
export class EnrollmentLessonsComponent implements OnInit {

  route = inject(ActivatedRoute);
  enrollmentWithCourseDataService = inject(EnrollmentWithCourseDataService);

  enrollmentId?: number;
  enrollmentWithCourse$!: Observable<EnrollmentWithCourseDto | null>;

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.enrollmentId = params['id'];
    });

    this.enrollmentWithCourse$ = this.enrollmentWithCourseDataService.enrollmentWithCourse$;
  }

  isCompleted(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.find(lp => lp.lessonId === lessonId)?.completed;
  }

  inProgress(lessonId: number, lessonProgresses: LessonProgress[]) {
    return lessonProgresses.find(lp => lp.lessonId === lessonId)?.inProgress;
  }

  isLessonBonus(lessonId: number, lessonProgresses: LessonProgress[]) {
    const lessonProgress = lessonProgresses.find(lp => lp.lessonId === lessonId);
    return lessonProgress ? lessonProgress.bonus : true;
  }

}
