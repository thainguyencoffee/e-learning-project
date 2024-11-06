import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import { Post } from "../../model/view/post";
import { DatePipe, NgForOf, NgIf, SlicePipe } from "@angular/common";
import { UserService } from "../../../../common/auth/user.service";
import { CourseService } from "../../service/course.service";
import { ErrorHandler } from "../../../../common/error-handler.injectable";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {Course} from "../../model/view/course";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
    DatePipe,
    SlicePipe
  ],
  templateUrl: './post-list.component.html',
})
export class PostListComponent implements OnInit,OnDestroy {
  router = inject(Router);
  route = inject(ActivatedRoute);
  courseService = inject(CourseService);
  userService = inject(UserService);
  errorHandler = inject(ErrorHandler)
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  courseId?: number;
  posts: Post[] = [];
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

  onPageChange(pageNumber: number): void {
    if (pageNumber >= 0 && pageNumber < this.paginationUtils!.totalPages) {
      this.loadData(pageNumber);
    }
  }
  isCreateByYou(teacher: string) {
    return this.userService.current.name === teacher;
  }


  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }
  loadData(pageNumber: number): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.courseService.getAllPosts(pageNumber, this.courseId!).subscribe({
      next: (pageWrapper) => {
        this.paginationUtils = new PaginationUtils(pageWrapper.page);
        this.posts = pageWrapper.content as Post[]; // Đảm bảo posts là một mảng
      },
      error: (error) => {
        console.error('Error loading posts:', error);
        this.errorHandler.handleServerError(error.error);
      }
    });
  }
  confirmDelete(postId: number,post: Post) {
    if (confirm(this.getMessage('confirmDelete'))) {
      this.courseService.deletePost(this.courseId!,postId,post)
        .subscribe({
          next: () => this.router.navigate(['/administration/courses', this.courseId, 'posts'], {
            state: {
              msgSuccess: this.getMessage('deleted')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });

    }

  }
  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirmDelete: 'Do you really want to delete this post?',
      deleted: 'Post was removed successfully.',
      confirmPublish: 'Do you really want to publish this course?. ' +
        'Before publishing a course, you need to agree to the system requirements in the terms and conditions. ' +
        'Are you sure you want to publish this course?',
      published: 'Course was published successfully.',
    };
    return messages[key];
  }
}
