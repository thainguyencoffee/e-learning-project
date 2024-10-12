import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {CourseDto} from "../../model/course.dto";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
  ],
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css'
})
export class CourseDetailComponent implements OnInit{

  route = inject(ActivatedRoute)
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  currentId?: number;
  courseDto?: CourseDto

  ngOnInit(): void {
    this.currentId = +this.route.snapshot.params['id']
    this.courseService.getCourse(this.currentId)
      .subscribe({
        next: (data) => this.courseDto = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

}
