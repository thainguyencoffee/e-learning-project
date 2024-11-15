import {Component, inject, OnInit} from '@angular/core';
import {AsyncPipe, DatePipe, NgClass, NgForOf, NgIf, NgStyle} from "@angular/common";
import {ActivatedRoute, NavigationEnd, Router, RouterLink, RouterOutlet} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {CourseService} from "../../../../administration/courses/service/course.service";
import {Comment, Emotion, Post} from "../../../../administration/courses/model/view/post";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {Subscription} from "rxjs";
import {UserService} from "../../../../common/auth/user.service";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {ArrayRowComponent} from "../../../../common/input-row/array/array-row.component";
import {FieldConfiguration} from "../../../../common/input-row/field-configuration";
import {CommentDto} from "../../../model/comment.dto";
import {updateFormAdvanced} from "../../../../common/utils";

@Component({
  selector: 'app-enrolment-posts',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    InputRowComponent,
    ArrayRowComponent,
    RouterLink
  ],
  templateUrl: './enrolment-posts.component.html',
})
export class EnrolmentPostsComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);
  paginationUtils?: PaginationUtils

  enrolmentId?: number;
  courseId?: number;
  navigationSubscription?: Subscription;

  posts: Post[] = [];

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  loadData(pageNumber: number) {
    this.enrolmentId = +this.route.snapshot.params['id'];
    const courseIdString = this.route.snapshot.queryParamMap.get('courseId');
    this.courseId = courseIdString ? +courseIdString : undefined;

    if (courseIdString) {
      this.courseId = +courseIdString;
      this.courseService.getAllPosts(pageNumber, this.courseId!, 5)
        .subscribe({
          next: pageWrapper => {
            this.paginationUtils = new PaginationUtils(pageWrapper.page);
            this.posts = pageWrapper.content;
          },
          error: error => this.errorHandler.handleServerError(error.error)
        })
    } else {
      this.router.navigate(['/error']);
    }
  }

  isImage(attachmentUrl: string) {
    return attachmentUrl.endsWith('.png') ||
      attachmentUrl.endsWith('.jpg') ||
      attachmentUrl.endsWith('.jpeg') ||
      attachmentUrl.endsWith('.gif') ||
      attachmentUrl.endsWith('.webp');
  }

  isVideo(attachmentUrl: string) {
    return attachmentUrl.endsWith('.mp4');
  }

  isLiked(emotions: Emotion[]) {
    if (emotions.length === 0) return false;
    const username = this.userService.current.name;
    return emotions.some(emotion => emotion.username === username);
  }

  onLike(postId: number) {
    this.courseService.addEmotion(this.courseId!, postId)
      .subscribe({
        next: () => this.loadData(0),
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDeleteComment: `Do you really want to remove this comment?`,
      confirmDelete: `Do you really want to delete this post?`,
      deleted: `Post has been deleted`
    }
    return messages[key];
  }

  attachmentUrlFieldConfigurationGeneral: FieldConfiguration = {
    type: 'imageFile',
    placeholder: 'Attachment URL'
  };

  createAttachmentUrlGeneral(): FormControl {
    return new FormControl(null, [Validators.required]);
  }

  addCommentForm = new FormGroup({
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(10000)]),
    attachmentUrls: new FormArray([])
  });

  get attachmentUrlsFromAddForm(): FormArray {
    return this.addCommentForm.get('attachmentUrls') as FormArray;
  }

  addAttachmentToAddForm(): void {
    this.attachmentUrlsFromAddForm.push(this.createAttachmentUrlGeneral());
  }

  removeAttachmentFromAddForm(index: number): void {
    this.attachmentUrlsFromAddForm.removeAt(index);
  }

  handleSubmitAddComment(postId: number) {
    this.addCommentForm.markAllAsTouched();
    if (!this.addCommentForm.valid) {
      return;
    }

    const data = new CommentDto(this.addCommentForm.value);
    console.log(data)
    this.addCommentForm.reset();
    this.attachmentUrlsFromAddForm.clear();

    this.courseService.addComment(this.courseId!, postId, data).subscribe({
      next: () => this.loadData(0),
      error: error => this.errorHandler.handleServerError(error.error)
    });
  }

  ownComment(comment: Comment) {
    return comment.info.username === this.userService.current.name;
  }

  /*Delete comment section*/

  deleteComment(postId: number, commentId: number) {
    if (confirm(this.getMessage('confirmDeleteComment'))) {
      this.courseService.deleteComment(this.courseId!, postId, commentId)
        .subscribe({
          next: () => this.loadData(0),
          error: error => this.errorHandler.handleServerError(error.error)
        })
    }
  }

  /*Edit comment section*/

  editCommentForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    content: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(10000)]),
    attachmentUrls: new FormArray([])
  });

  get attachmentUrlsFromEditForm(): FormArray {
    return this.editCommentForm.get('attachmentUrls') as FormArray;
  }

  addAttachmentToEditForm(): void {
    this.attachmentUrlsFromEditForm.push(this.createAttachmentUrlGeneral());
  }

  removeAttachmentFromEditForm(index: number): void {
    this.attachmentUrlsFromEditForm.removeAt(index);
  }

  enableEdit(comment: Comment) {
    comment.isEditing = true;
    updateFormAdvanced(this.editCommentForm, comment, this.createAttachmentUrlGeneral);
  }

  handleSubmitEditComment(postId: number, comment: Comment) {
    this.editCommentForm.markAllAsTouched();
    if (!this.editCommentForm.valid) {
      return;
    }

    const data = new CommentDto(this.editCommentForm.value);
    this.courseService.editComment(this.courseId!, postId, comment.id, data)
      .subscribe({
        next: () => {
          this.loadData(0);
          comment.isEditing = false;
        },
        error: error => this.errorHandler.handleServerError(error.error, this.editCommentForm)
      })
  }

  cancelEdit(comment: Comment) {
    comment.isEditing = false;
  }

  isAdminOrTeacher() {
    return this.userService.current.hasAnyRole('ROLE_admin', 'ROLE_teacher');
  }

  confirmDelete(postId: number, post: Post) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.courseService.deletePost(this.courseId!,postId,post)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses', this.courseId, 'posts', 'trash'], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }
  }

}
