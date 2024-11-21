import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {NgClass, NgForOf, NgIf, SlicePipe} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";
import {CourseRequest} from "../../model/view/course-request";

@Component({
  selector: 'app-request-list',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    NgForOf,
    SlicePipe,
    NgClass,
  ],
  templateUrl: './request-list.component.html',
})
export class RequestListComponent implements OnInit {

  route = inject(ActivatedRoute);
  courseService = inject(CourseService);
  userService = inject(UserService)

  courseId?: number;
  course?: Course
  expandMessageFlags: Record<number, boolean> = {};

  ngOnInit(): void {
    this.courseId = this.route.snapshot.params['courseId'];

    this.courseService.getCourse(this.courseId!).subscribe({
      next: data => {
        this.course = data;
        this.initFlagArrayForExpandMessage(this.course?.courseRequests);
      },
    })
  }

  canResolveRequest(request: CourseRequest) {
    return this.userService.current.hasAnyRole('ROLE_admin') && !request.resolved
  }

  //public boolean isPublishedAndNotUnpublishedOrDelete() {
  //   return published && !unpublished || deleted;
  //}
  canRequestPublish(course: Course) {
    return this.userService.current.hasAnyRole('ROLE_teacher')
      && this.isTitleBlue(course) && (!course.published || course.unpublished);
  }

  // public boolean isPublishedAndNotUnpublishedOrDelete() {
  //   return published && !unpublished || deleted;
  // }
  canRequestUnPublish(course: Course) {
    return this.userService.current.hasAnyRole('ROLE_teacher')
      && this.isTitleGreen(course) && course.published && !course.unpublished;
  }

  private isTitleGreen(course: Course) {
    return this.isTitleBlue(course) && course.price;
  }

  private isTitleBlue(course: Course) {
    if (course.sections && course.sections.length > 0) {
      for (const section of course.sections) {
        return section.lessons && section.lessons.length > 0;
      }
    }
    return false;
  }

  private initFlagArrayForExpandMessage(courseRequests: CourseRequest[] | undefined) {
    if (courseRequests) {
      courseRequests.forEach(request => {
        this.expandMessageFlags[request.id] = false;
      });
    }
  }
}
