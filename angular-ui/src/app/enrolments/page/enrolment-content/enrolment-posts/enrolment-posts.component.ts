import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {AsyncPipe, DatePipe, NgClass, NgForOf, NgIf, NgStyle} from "@angular/common";
import {ActivatedRoute, NavigationEnd, Router, RouterLink, RouterOutlet} from "@angular/router";
import {FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Subscription} from "rxjs";
import {Post} from "../../../../administration/courses/model/view/post";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {Comment} from '../../../../administration/courses/model/view/comment';
import {FieldConfiguration} from "../../../../common/input-row/field-configuration";
import {ArrayRowComponent} from "../../../../common/input-row/array/array-row.component";
import {Emotion} from "../../../../administration/courses/model/view/emotion";
import {UserService} from "../../../../common/auth/user.service";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {CourseService} from "../../../../administration/courses/service/course.service";
import {CommentDto} from "../../../model/comment-dto";


@Component({
  selector: 'app-enrolment-posts',
  standalone: true,
  styleUrls: ['enrolment-posts.component.css'],
  imports: [
    NgForOf,
    NgClass,
    RouterLink,
    RouterOutlet,
    NgIf,
    AsyncPipe,
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    InputRowComponent,
    ArrayRowComponent,
    NgStyle
  ],
  templateUrl: './enrolment-posts.component.html',
})
export class EnrolmentPostsComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  courseService = inject(CourseService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler);

  commentToggles: { [postId: number]: boolean } = {}; // Toggle comment visibility

  enrolmentId?: number;
  courseId?: number;
  post?: Post;
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  commentIsOpen = false;

  addCommentForm = new FormGroup({
    content: new FormControl(null, [Validators.minLength(1), Validators.maxLength(10000)]),
    attachmentUrls: new FormArray([])
  });

  attachmentUrlFieldConfiguration: FieldConfiguration = {
    type: 'imageFile',
    placeholder: 'Attachment URL'
  };

  get attachmentUrls(): FormArray {
    return this.addCommentForm.get('attachmentUrls') as FormArray;
  }

  createAttachmentUrl(): FormControl {
    return new FormControl(null, [Validators.required]);
  }

  addAttachment(): void {
    this.attachmentUrls.push(this.createAttachmentUrl());
  }

  removeAttachment(index: number): void {
    this.attachmentUrls.removeAt(index);
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      commented: 'Comment was added successfully.',
      liked: `You liked this post.`,
    };
    return messages[key];
  }

  ngOnInit(): void {
    this.loadData(0);

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  private loadData(pageNumber: number) {
    this.enrolmentId = +this.route.snapshot.params['id'];
    this.courseId = +this.route.snapshot.params['courseId'];

    this.courseService.getAllPosts(pageNumber, this.courseId, 1).subscribe({
      next: (pageWrapper) => {
        this.paginationUtils = new PaginationUtils(pageWrapper.page);
        const posts = pageWrapper.content as Post[];
        if (posts.length > 0) {
          this.post = posts[0];
        }
      }
    })
  }

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

  handleSubmit(postId: number) {
    window.scrollTo(0, 0);
    this.addCommentForm.markAllAsTouched();
    if (!this.addCommentForm.valid) {
      return;
    }

    const data = new CommentDto(this.addCommentForm.value);

    this.enrolmentService.addComment(this.courseId!, postId, data).subscribe({
      next: () => this.router.navigate(['../'], {
        relativeTo: this.route,
        state: {
          msgSuccess: this.getMessage('commented')
        }
      }),
      error: (error) => this.errorHandler.handleServerError(error.error, this.addCommentForm, this.getMessage)
    });
  }

  setCommentDisplayMode(post: Post, showAll: boolean): void {
    if (showAll) {
      post.comments = [...post.comments];
    } else {
      post.comments = post.comments?.sort((b,a ) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()) || [];
    }
  }

  // Return all comments or recent comments based on display mode
  getDisplayComments(post: Post): Comment[] {
    if (this.commentIsOpen) {
      return post.comments || [];
    } else {
      return post.comments?.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()).slice(0, 2) || [];
    }
  }

  toggleComments(postId: number): void {
    this.commentToggles[postId] = !this.commentToggles[postId];
  }

  toggleEmotion(post: Post): void {
    const currentUsername = this.userService.current.name;

    const userEmotion = post.emotions?.find(e => e.username === currentUsername);

    if (userEmotion) {
      // Deleting the existing emotion
      if (userEmotion.id !== undefined) {
        this.enrolmentService.deleteEmotion(this.courseId!, post.id, userEmotion.id).subscribe({
          next: _ => this.router.navigate(['.'], {
            relativeTo: this.route
          }),
          error: error => this.errorHandler.handleServerError(error)
        });
      }
    } else {
      // Adding a new emotion with the current user's username
      this.enrolmentService.addEmotion(this.courseId!, post.id, currentUsername).subscribe({
        next: _ => this.router.navigate(['.'], {
          relativeTo: this.route
        }),
        error: error => this.errorHandler.handleServerError(error.error)
      });
    }
  }

  isLiked(emotions: Emotion[]): boolean {
    const currentUsername = this.userService.current.name;
    return emotions?.some((emotion: Emotion) => emotion.username === currentUsername) ?? false;
  }

}
