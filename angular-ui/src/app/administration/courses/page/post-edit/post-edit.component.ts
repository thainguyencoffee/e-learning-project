import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { CourseService } from "../../service/course.service";
import { ErrorHandler } from "../../../../common/error-handler.injectable";
import { FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { InputRowComponent } from "../../../../common/input-row/input-row.component";
import { PostDto } from "../../model/post-dto";
import {updateForm} from "../../../../common/utils";

@Component({
  selector: 'app-edit-post',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './post-edit.component.html',
})
export class EditPostComponent implements OnInit {

  courseService = inject(CourseService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);

  courseId?: number;
  postId?: number;

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    content: new FormControl(null, [Validators.required, Validators.maxLength(2000)]),
    photoUrls: new FormControl(null) // Change to photoUrls for multiple images
  });


  getMessage(key: string) {
    const messages: Record<string, string> = {
      updated: `Post was updated successfully.`,
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.postId = +this.route.snapshot.params['postId'];

    // Tải dữ liệu bài viết để chỉnh sửa
    this.courseService.getPost(this.courseId!, this.postId!)
      .subscribe({
       next: (data) => updateForm(this.editForm, data),
      error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }
  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new PostDto(this.editForm.getRawValue());

    this.courseService.updatePost(this.courseId!, this.postId!, data)
      .subscribe({
        next: () => this.router.navigate(['/administration/courses', this.courseId, 'posts'], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });
  }
}
