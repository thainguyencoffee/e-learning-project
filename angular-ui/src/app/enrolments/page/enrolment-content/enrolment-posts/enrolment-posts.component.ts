import {Component, inject, OnInit} from '@angular/core';
import {AsyncPipe, DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {ActivatedRoute, Router, RouterLink, RouterOutlet} from "@angular/router";
import {FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {EnrolmentWithCourseDataService} from "../enrolment-with-course-data.service";
import {Post} from "../../../../administration/courses/model/view/post";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {PostDto} from "../../../../administration/courses/model/post-dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";
import {ArrayRowComponent} from "../../../../common/input-object-row/array-row.component";
import {FieldConfiguration} from "../../../../common/input-object-row/field-configuration";
import {AddCourseDto} from "../../../../administration/courses/model/add-course.dto";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {Comment} from '../../../../administration/courses/model/view/comment';


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
    ArrayRowComponent
  ],
  templateUrl: './enrolment-posts.component.html',
})
export class EnrolmentPostsComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  enrolmentWithCourseDataService = inject(EnrolmentWithCourseDataService);
  currentPage = 0;
  enrolmentId?: number;
  courseId!: number;
  username?: string; // Ví dụ, username có thể lấy từ JWT hoặc session
  postId!: number;
  commentToggles: { [postId: number]: boolean } = {}; // Toggle comment visibility
  enrolmentWithCourse$!: Observable<EnrolmentWithCourseDto | null>;
  posts$!: Observable<Post[]>;

  displayAllComments: boolean = false;
  comments = [ /* Mảng các bình luận với ngày tạo */ ];

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
      created: `Comment was add successfully.`
    };
    return messages[key];
  }

  handleSubmit(postId: number): void {
    window.scrollTo(0, 0);
    if (!this.addCommentForm.valid) {
      return;
    }

    const commentData = this.addCommentForm.value;
    this.enrolmentService.addComment(this.courseId, postId, commentData).subscribe({
      next: () => {
        // Điều hướng lại đến trang hiện tại để tải lại
        this.router.navigate([`/enrolments/${this.courseId}/posts`], {
          queryParamsHandling: 'preserve',  // Giữ lại các query params nếu có
          state: { msgSuccess: this.getMessage('created') }
        });
      },
      error: (error) => {
        this.errorHandler.handleServerError(error.error, this.addCommentForm);
      }
    });
  }


  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.courseId = +params['id'];
      this.postId = +params['id'];
      this.enrolmentId = +params['id'];
    });

    this.enrolmentWithCourse$ = this.enrolmentWithCourseDataService.enrolmentWithCourse$;

    this.posts$ = this.enrolmentWithCourse$.pipe(
      map(enrolment => {
        console.log('Enrolment data:', enrolment);
        return enrolment?.posts || [];
      })
    );
  }

  setCommentDisplayMode(showAll: boolean): void {
    this.displayAllComments = showAll;
    this.posts$.subscribe(posts => {
      posts.forEach(post => this.filterComments(post));
    });
  }
  filterComments(post: Post): void {
    if (this.displayAllComments) {
      // Hiển thị tất cả bình luận
      post.comments = [...post.comments];
    } else {
      post.comments = post.comments?.sort((b,a ) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()) || [];
    }
  }

// Return all comments or recent comments based on display mode
  getDisplayComments(post: Post): Comment[] {
    if (this.displayAllComments) {
      return post.comments || [];
    } else {
      return post.comments?.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()).slice(0, 2) || [];
    }
  }


  toggleComments(postId: number): void {
    this.commentToggles[postId] = !this.commentToggles[postId];
  }

  getCurrentPagePost(posts: Post[]): Post | undefined {
    return posts[this.currentPage];
  }

  previousPage(posts: Post[]): void {
    if (this.currentPage > 0) this.currentPage--;
  }

  nextPage(posts: Post[]): void {
    if (this.currentPage < posts.length - 1) this.currentPage++;
  }
}
