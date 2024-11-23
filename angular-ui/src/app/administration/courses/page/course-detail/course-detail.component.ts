import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {Subscription} from "rxjs";
import {UserService} from "../../../../common/auth/user.service";
import {Section} from "../../model/view/section";
import {Lesson} from "../../model/view/lesson";
import {EnrollmentsService} from "../../../../enrollment/service/enrollments.service";

@Component({
  selector: 'app-course-detail',
  standalone: true,
  styleUrls: ['course-detail.component.css'],
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
    DatePipe,
    NgClass,
  ],
  templateUrl: './course-detail.component.html',
})
export class CourseDetailComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute)
  router = inject(Router);
  courseService = inject(CourseService);
  enrollmentService = inject(EnrollmentsService);
  errorHandler = inject(ErrorHandler);
  userService = inject(UserService);
  selectedSection?: Section | null;
  currentId?: number;
  courseDto?: Course
  participantNumber?: number;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData()

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  selectSection(section: any) {
    this.selectedSection = section;
  }

  loadData() {
    this.currentId = +this.route.snapshot.params['id']
    this.courseService.getCourse(this.currentId)
      .subscribe({
        next: (data) => {
          this.courseDto = data;
          this.courseDto.sections?.sort((a, b) => a.orderIndex - b.orderIndex);
          this.courseDto.sections?.forEach(section => section.lessons.sort((a, b) => a.orderIndex - b.orderIndex));
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })

    this.enrollmentService.countEnrollmentsByCourseId(this.currentId)
      .subscribe({
        next: (data) => this.participantNumber = data,
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: 'Do you really want to delete this element?',
    }
    return messages[key];
  }

  confirmDeleteSection(sectionId: number, section: Section) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteSection(this.currentId!, sectionId, section)
        .subscribe({
          next: () => {
            this.loadData()
            this.selectedSection = null
          },
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }

  confirmDeleteLesson(sectionId: number, lessonId: number, lesson: Lesson) {
    if (confirm(this.getMessage('confirm'))) {
      this.courseService.deleteLesson(this.currentId!, sectionId, lessonId, lesson)
        .subscribe({
          next: () => this.loadData(),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    }

  }

  isCreateByYou(teacher: string) {
    return this.userService.current.name === teacher;
  }



  getIdRequestUnresolved() {
    if (this.courseDto?.courseRequests && this.userService.current.hasAnyRole('ROLE_admin')) {
      return this.courseDto?.courseRequests.find(request => !request.resolved)?.id;
    }
    return null;
  }

  getQuizByLessonId(selectedSection: Section, lessonId: number) {
    return selectedSection.quizzes?.find(quiz => quiz.afterLessonId === lessonId);
  }

  isUnpublishedMode(course: Course, section: Section) {
    return course.unpublished && section.published
  }

  isEdit(courseDto: Course, selectedSection: Section) {
    return !courseDto.published || (courseDto.unpublished && !selectedSection.published);
  }
}
