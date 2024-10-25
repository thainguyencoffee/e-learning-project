import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {NgForOf, NgIf} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";
import {CourseRequest} from "../../model/view/course-request";

@Component({
  selector: 'app-request-list',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf
  ],
  templateUrl: './request-list.component.html',
})
export class RequestListComponent implements OnInit {

  route = inject(ActivatedRoute);
  courseService = inject(CourseService);
  userService = inject(UserService)

  courseId?: number;
  course?: Course

  ngOnInit(): void {
    this.courseId = this.route.snapshot.params['courseId'];

    this.courseService.getCourse(this.courseId!).subscribe({
      next: data => this.course = data,
    })
  }

  canResolveRequest(request: CourseRequest) {
    return this.userService.current.hasAnyRole('ROLE_admin') && !request.resolved
  }

  canRequestPublish(course: Course) {
    return this.userService.current.hasAnyRole('ROLE_teacher')
      && this.isTitleBlue(course) && !course.published;
  }

  canRequestUnPublish(course: Course) {
    return this.userService.current.hasAnyRole('ROLE_teacher')
      && this.isTitleGreen(course) && this.noStudentsInCourse(course) && course.published;
  }

  private noStudentsInCourse(course: Course) {
    return course.students.length === 0;
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

}
