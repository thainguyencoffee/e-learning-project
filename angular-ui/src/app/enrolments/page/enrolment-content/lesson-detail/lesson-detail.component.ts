import {AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Observable} from "rxjs";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {AsyncPipe, NgIf} from "@angular/common";
import {Lesson} from "../../../../administration/courses/model/view/lesson";
import {Section} from "../../../../administration/courses/model/view/section";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {error} from "@angular/compiler-cli/src/transformers/util";
import videojs from "video.js";
import {VideoPlayerComponent} from "../../../../common/video-player/video-player.component";

@Component({
  selector: 'app-lesson-detail',
  standalone: true,
  imports: [
    RouterLink,
    AsyncPipe,
    NgIf,
    VideoPlayerComponent
  ],
  templateUrl: './lesson-detail.component.html',
})
export class LessonDetailComponent implements OnInit {

  route = inject(ActivatedRoute);
  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  lessonId?: number;
  enrolmentId?: number;
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;

  ngOnInit(): void {
    this.enrolmentId = +this.route.snapshot.params['enrolmentId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => this.enrolmentWithCourseDataService.setEnrolmentWithCourse(data),
        error: (error) => this.errorHandler.handleServerError(error.error)
      })

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;
  }

  getLessonById(lessonId: number, sections: Section[]) {
    return sections.flatMap(s => s.lessons).find(l => l.id === lessonId);
  }

}
