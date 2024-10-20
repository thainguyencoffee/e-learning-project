import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {NgForOf} from "@angular/common";
import {UsersService} from "../../../../common/auth/users.service";
import {UserInfo} from "../../../../common/auth/user-info";
import {FormsModule} from "@angular/forms";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {UserService} from "../../../../common/auth/user.service";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";

@Component({
  selector: 'app-assign-teacher',
  standalone: true,
  imports: [
    NgForOf,
    FormsModule,
    RouterLink
  ],
  templateUrl: './assign-teacher.component.html',
})
export class AssignTeacherComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  usersService = inject(UsersService);
  userService = inject(UserService);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler)

  courseId?: number;
  username?: string = '';
  teachers: UserInfo[] = [];
  course?: Course;

  ngOnInit(): void {
    this.courseId = this.route.snapshot.params['courseId'];
    this.courseService.getCourse(this.courseId!).subscribe({
      next: data => this.course = data,
      error: error => this.errorHandler.handleServerError(error.error)
    })
  }

  search() {
    if (this.username && this.username.trim()) {
      this.usersService.searchByUsername(this.username).subscribe({
        next: data => this.teachers = data,
        error: error => this.errorHandler.handleServerError(error.error)
      })
    }

  }

  canAssignable(teacherId: string) {
    return this.userService.current.name !== teacherId && this.course?.teacher !== teacherId;
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Course was assigned successfully.`
    };
    return messages[key];
  }

  assignTeacher(teacherId: string) {
    this.courseService.assignTeacher(this.courseId!, teacherId)
      .subscribe({
        next: _ => this.router.navigate(['/administration/courses', this.courseId], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }
}
