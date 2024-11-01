import {Component, inject, OnInit} from '@angular/core';
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {ActivatedRoute, NavigationEnd, Router, RouterLink, RouterOutlet} from "@angular/router";
import {EnrolmentWithCourseDto} from "../../../model/enrolment-with-course-dto";
import {Subscription} from "rxjs";
import {EnrolmentsService} from "../../../service/enrolments.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
@Component({
  selector: 'app-enrolment-posts',
  standalone: true,
  styleUrls:['enrolment-posts.component.css'],
  imports: [
    NgForOf,
    NgClass,
    RouterLink,
    RouterOutlet,
    NgIf,
    AsyncPipe
  ],
  templateUrl: './enrolment-posts.component.html',
})
export class EnrolmentPostsComponent implements OnInit{
   route = inject(ActivatedRoute);
  router = inject(Router);
  enrolmentService = inject(EnrolmentsService);
  errorHandler = inject(ErrorHandler);

  enrolmentId?: number;
  enrolmentWithCourse?: EnrolmentWithCourseDto;
  navigationSubscription?: Subscription;
  enrolment = {
    courseId: 101,
    title: 'Lập Trình JavaScript Từ Cơ Bản Đến Nâng Cao',
    thumbnailUrl: 'https://example.com/js-course-thumbnail.jpg',
    description: 'Khóa học cung cấp nền tảng từ căn bản đến chuyên sâu về JavaScript, bao gồm ES6, DOM Manipulation, và Asynchronous Programming. Hoàn thành khóa học, bạn sẽ có thể tự tin xây dựng các ứng dụng web với JavaScript.',
    language: 'Tiếng Việt',
    subtitles: ['Tiếng Anh', 'Tiếng Việt'],
    benefits: [
      'Nắm vững kiến thức JavaScript từ cơ bản đến nâng cao.',
      'Thực hành qua các dự án thực tế giúp củng cố kỹ năng lập trình.',
      'Được hỗ trợ bởi cộng đồng học viên và giảng viên.'
    ],
    prerequisites: ['Kiến thức cơ bản về HTML và CSS'],
    sections: [
      {
        id: 1,
        title: 'Giới thiệu về JavaScript',
        description: 'Hiểu về cú pháp JavaScript cơ bản, các kiểu dữ liệu, và các hàm cơ bản.',
        lessons: [
          { title: 'Bài học 1: Khái niệm cơ bản về JavaScript' },
          { title: 'Bài học 2: Kiểu dữ liệu và biến' }
        ]
      },
      {
        id: 2,
        title: 'JavaScript nâng cao',
        description: 'ES6, lập trình hướng đối tượng (OOP), và xử lý bất đồng bộ.',
        lessons: [
          { title: 'Bài học 3: Giới thiệu ES6' },
          { title: 'Bài học 4: Lập trình hướng đối tượng với JavaScript' },
          { title: 'Bài học 5: Async và Promises' }
        ]
      },
      {
        id: 3,
        title: 'Ứng dụng JavaScript trong dự án thực tế',
        description: 'Xây dựng một ứng dụng web hoàn chỉnh sử dụng JavaScript.',
        lessons: [
          { title: 'Bài học 6: Setup dự án' },
          { title: 'Bài học 7: Hoàn thiện ứng dụng' }
        ]
      }
    ],
    teacher: 'Nguyễn Văn A',
    enrollmentId: 123456,
    student: 'Trần B',
    lessonProgresses: [
      { title: 'Giới thiệu về JavaScript', progress: 100 },
      { title: 'Kiểu dữ liệu và biến', progress: 80 },
      { title: 'Hàm và đối tượng', progress: 60 }
    ],
    completed: false
  };
  ngOnInit(): void {
    this.loadData();
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }
  loadData(): void {
    this.enrolmentId = +this.route.snapshot.params['id'];
    this.enrolmentService.getEnrolmentWithCourseByEnrollmentId(this.enrolmentId)
      .subscribe({
        next: (data) => this.enrolmentWithCourse = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }
  protected readonly JSON = JSON;
}
