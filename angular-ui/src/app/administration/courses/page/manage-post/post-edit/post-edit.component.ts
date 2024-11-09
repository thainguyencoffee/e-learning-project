import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { CourseService } from "../../../service/course.service";
import { ErrorHandler } from "../../../../../common/error-handler.injectable";
import { FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { InputRowComponent } from "../../../../../common/input-row/input-row.component";
import { PostDto } from "../../../model/post-dto";
import {updateFormAdvanced} from "../../../../../common/utils";
import {FieldConfiguration} from "../../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../../common/input-row/array/array-row.component";

@Component({
  selector: 'app-edit-post',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule,
    ArrayRowComponent
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

  attachmentUrlFieldConfiguration: FieldConfiguration = {
    type: 'imageFile',
    placeholder: 'Attachment URL'
  }

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(10000)]),
    attachmentUrls: new FormArray([], [Validators.required])
  });

  get attachmentUrls() {
    return this.editForm.get('attachmentUrls') as FormArray;
  }

  createAttachmentUrl() {
    return new FormControl(null, [Validators.required]);
  }

  addAttachment() {
    this.attachmentUrls.push(this.createAttachmentUrl());
  }

  removeAttachment(index: number) {
    this.attachmentUrls.removeAt(index);
  }

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
       next: (data) => {
         updateFormAdvanced(this.editForm, data, this.createAttachmentUrl)
       },
      error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new PostDto(this.editForm.value);

    this.courseService.updatePost(this.courseId!, this.postId!, data)
      .subscribe({
        next: () => this.router.navigate(['../'], {
          relativeTo: this.route,
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });
  }
}
