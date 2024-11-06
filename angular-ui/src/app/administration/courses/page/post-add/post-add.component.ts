import {Component, inject, OnInit} from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import { PostDto } from "../../model/post-dto"; // Adjust import according to your structure
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { ErrorHandler } from "../../../../common/error-handler.injectable";
import { InputRowComponent } from "../../../../common/input-row/input-row.component";
import { CourseService } from "../../service/course.service";
import { NgIf } from "@angular/common";

@Component({
  selector: 'app-post-add',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent,
    NgIf
  ],
  templateUrl: './post-add.component.html',
})
export class PostAddComponent implements OnInit{
  router = inject(Router);
  courseId?: number;
  route = inject(ActivatedRoute);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  addForm = new FormGroup({
    content: new FormControl(null, [Validators.required]),
    photoUrls: new FormControl(null)// Change to photoUrls for multiple images
  });
  ngOnInit(): void {
    // Lấy courseId từ tham số URL khi component được khởi tạo
    this.courseId = +this.route.snapshot.params['courseId'];
    console.log('Course ID:', this.courseId); // Kiểm tra courseId có được gán đúng không
  }
  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }
    const data = new PostDto(this.addForm.value);
    const courseId = this.route.snapshot.params['courseId'];
    console.log(data)
    this.courseService.createPost(data, courseId).subscribe({
      next: () => this.router.navigate(['/administration/courses', courseId, 'posts'], {
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    });
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Post was created successfully.`
    };
    return messages[key];
  }
}
