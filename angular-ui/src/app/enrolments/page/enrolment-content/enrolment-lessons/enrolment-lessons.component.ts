import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {from, mergeMap, Observable, toArray} from "rxjs";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {Section} from "../../../../administration/courses/model/view/section";
import {Lesson} from "../../../../administration/courses/model/view/lesson";

@Component({
  selector: 'app-enrolment-lessons',
  standalone: true,
  imports: [
    AsyncPipe,
    NgIf,
    NgForOf,
    NgClass
  ],
  templateUrl: './enrolment-lessons.component.html',
  styleUrl: './enrolment-lessons.component.css'
})
export class EnrolmentLessonsComponent implements OnInit {

  route = inject(ActivatedRoute);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);

  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;
  lessons: Lesson[] = [];
  lessonMap: { [id: string]: Lesson } = {};

  ngOnInit(): void {
    this.enrolmentId = +this.route.snapshot.params['id'];

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  lessonEnabled(index: number, completedLesson: number) {
    return index <= completedLesson;
  }

  setupLessonsMap(sections: Section[]) {
    from(sections).pipe(
      mergeMap(section => section.lessons),
      toArray()
    ).subscribe({
      next: lessons => {
        this.lessons = lessons;
        this.lessonMap = lessons.reduce((map: { [id: string]: Lesson }, lesson) => {
          map[lesson.id] = lesson;
          return map;
        }, {});
      },
      error: _ => false
    });
    return true;
  }

}
