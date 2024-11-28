import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {CourseRequest} from "../../model/view/course-request";
import {DatePipe, NgClass, NgIf} from "@angular/common";
import {UserService} from "../../../../common/auth/user.service";

@Component({
  selector: 'app-request-course-detail',
  standalone: true,
  imports: [
    NgIf,
    RouterLink,
    NgClass,
    DatePipe
  ],
  templateUrl: './request-course-detail.component.html',
})
export class RequestCourseDetailComponent implements OnInit{

  route = inject(ActivatedRoute);
  courseService = inject(CourseService)
  userService = inject(UserService);

  courseId?: number;
  requestId?: number;
  requestCourse?: CourseRequest;
  resourceUrl?: string;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = params['courseId'];
      this.requestId = params['requestId'];
    });
    this.resourceUrl = `/administration/courses/${this.courseId}/requests`

    this.courseService.getCourse(this.courseId!).subscribe({
      next: course => {
        this.requestCourse = course.courseRequests?.find(request => request.id == this.requestId)
      }
    })
  }

  canResolveRequest(request: CourseRequest) {
    return this.userService.current.hasAnyRole('ROLE_admin') && !request.resolved
  }

}
