import { Component, inject } from '@angular/core';
import { AsyncPipe, DatePipe, NgClass, NgForOf, NgIf } from "@angular/common";
import { ActivatedRoute, Router, RouterLink, RouterOutlet } from "@angular/router";
import { EnrolmentsService } from "../../../service/enrolments.service";
import { ErrorHandler } from "../../../../common/error-handler.injectable";
import { FormsModule } from "@angular/forms";
import { EnrolmentWithCourseDto } from "../../../model/enrolment-with-course-dto";

// Define the Comment interface
interface Comment {
  user: string;
  text: string;
  replies: Comment[]; // Allows for nested replies
}

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
    FormsModule
  ],
  templateUrl: './enrolment-posts.component.html',
})
export class EnrolmentPostsComponent {
  route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);
  enrolmentId?: number;
  enrolmentWithCourse?: EnrolmentWithCourseDto;
  teacher = {
    name: 'Dr. John Doe',
    department: 'Computer Science',
    avatarUrl: 'https://via.placeholder.com/50',
  };

  postsList = [
    {
      title: 'Tìm hiểu về Machine Learning',
      content: 'Machine Learning đang trở thành một công cụ mạnh mẽ trong công nghệ hiện đại. Hãy cùng khám phá...',
      date: new Date(),
      imageUrls: ['https://via.placeholder.com/150', 'https://via.placeholder.com/200']
    },// Array of image URLs    },
  ];

  // Comments section with Comment type
  likes = 0;
  liked = false;
  showComments = false;
  comments: Comment[] = [
    { user: 'Alice', text: 'Bài viết rất hữu ích, cảm ơn thầy!', replies: [] },
    { user: 'Bob', text: 'Mong thầy sẽ có thêm nhiều bài chia sẻ nữa.', replies: [] }
  ];
  newComment = '';

  toggleComments() {
    this.showComments = !this.showComments;
  }

  addComment() {
    if (this.newComment.trim()) {
      this.comments.push({ replies: [], user: 'Bạn', text: this.newComment });
      this.newComment = '';
    }
  }

  toggleLike() {
    this.likes += this.liked ? -1 : 1;
    this.liked = !this.liked;
  }

  // Function to add a reply to a comment
  addReply(comment: Comment) {
    if (this.newComment.trim()) {
      comment.replies.push({ user: 'Bạn', text: this.newComment, replies: [] });
      this.newComment = ''; // Clear the input
    }
  }
}
