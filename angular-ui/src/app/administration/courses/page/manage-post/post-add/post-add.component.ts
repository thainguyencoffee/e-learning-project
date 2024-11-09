import {Component, inject, OnInit} from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import { PostDto } from "../../../model/post-dto";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { ErrorHandler } from "../../../../../common/error-handler.injectable";
import { InputRowComponent } from "../../../../../common/input-row/input-row.component";
import { CourseService } from "../../../service/course.service";
import { NgIf } from "@angular/common";
import {FieldConfiguration} from "../../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../../common/input-row/array/array-row.component";

@Component({
  selector: 'app-post-add',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    InputRowComponent,
    NgIf,
    ArrayRowComponent
  ],
  templateUrl: './post-add.component.html',
})
export class PostAddComponent implements OnInit{

  router = inject(Router);
  courseId?: number;
  route = inject(ActivatedRoute);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Post was created successfully.`
    };
    return messages[key];
  }

  attachmentUrlFieldConfiguration: FieldConfiguration = {
    type: 'imageFile',
    placeholder: 'Attachment URL'
  }

  addForm = new FormGroup({
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(10000)]),
    attachmentUrls: new FormArray([], [Validators.required])
  });

  get attachmentUrls() {
    return this.addForm.get('attachmentUrls') as FormArray;
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

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    const data = new PostDto(this.addForm.value);

    this.courseService.createPost(data, this.courseId!).subscribe({
      next: () => this.router.navigate(['../'], {
        relativeTo: this.route,
        state: {
          msgSuccess: this.getMessage('created')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
    });
  }


}
