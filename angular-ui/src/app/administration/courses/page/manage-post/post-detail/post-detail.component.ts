import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, NavigationEnd, RouterLink } from "@angular/router";
import { PostDto } from "../../../model/post-dto";
import { ErrorHandler } from "../../../../../common/error-handler.injectable";
import { DatePipe, NgForOf, NgIf } from "@angular/common";
import { Subscription } from "rxjs";
import { UserService } from "../../../../../common/auth/user.service";
import { CourseService } from "../../../service/course.service";
import {Post} from "../../../model/view/post";

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
    DatePipe,
  ],
  templateUrl: './post-detail.component.html',
})
export class PostDetailComponent implements OnInit, OnDestroy {
  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler);

  courseId?:number;
  postId?: number;
  post?: Post;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadPost();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadPost();
      }
    });
  }

  ngOnDestroy(): void {
    this.navigationSubscription?.unsubscribe();
  }

  loadPost() {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.postId = +this.route.snapshot.params['postId'];

    this.courseService.getPost(this.courseId, this.postId)
      .subscribe({
        next: (data) => this.post = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      });
  }

}
