import { Routes } from '@angular/router';
import {BrowseCoursesComponent} from "./browse-course/page/browse-courses/browse-courses.component";
import {DashboardComponent} from "./administration/dashboard/dashboard.component";
import {ListCourseComponent} from "./administration/courses/page/list-course/list-course.component";
import {AddCourseComponent} from "./administration/courses/page/add-course/add-course.component";
import {EditCourseComponent} from "./administration/courses/page/edit-course/edit-course.component";
import { AddSectionComponent } from './administration/courses/page/add-section/add-section.component';
import {EditSectionComponent} from "./administration/courses/page/edit-section/edit-section.component";
import {AddLessonComponent} from "./administration/courses/page/add-lesson/add-lesson.component";
import {EditLessonComponent} from "./administration/courses/page/edit-lesson/edit-lesson.component";
import {AssignTeacherComponent} from "./administration/courses/page/assign-teacher/assign-teacher.component";
import {CourseTrashComponent} from "./administration/courses/page/course-trash/course-trash.component";
import {SetPriceComponent} from "./administration/courses/page/set-price/set-price.component";
import {ListDiscountComponent} from "./administration/discounts/page/list-discount/list-discount.component";
import {DiscountTrashComponent} from "./administration/discounts/page/discount-trash/discount-trash.component";
import {AddDiscountComponent} from "./administration/discounts/page/add-discount/add-discount.component";
import {EditDiscountComponent} from "./administration/discounts/page/edit-discount/edit-discount.component";
import {RequestCourseComponent} from "./administration/courses/page/request-course/request-course.component";
import {RequestListComponent} from "./administration/courses/page/request-list/request-list.component";
import {ResolveRequestComponent} from "./administration/courses/page/resolve-request/resolve-request.component";
import {
  RequestCourseDetailComponent
} from "./administration/courses/page/request-course-detail/request-course-detail.component";
import {CheckoutComponent} from "./orders/page/checkout/checkout.component";
import {PaymentComponent} from "./payment/page/payment.component";
import {MyOrdersComponent} from "./orders/page/my-orders/my-orders.component";
import {MyOrderDetailComponent} from "./orders/page/my-order-detail/my-order-detail.component";
import {MyEnrollmentsComponent} from "./enrollment/page/my-enrollments/my-enrollments.component";
import {BrowseCourseDetailComponent} from "./browse-course/page/browse-course-detail/browse-course-detail.component";
import {EnrollmentContentComponent} from "./enrollment/page/enrollment-content/enrollment-content.component";
import {CourseDetailComponent} from "./administration/courses/page/course-detail/course-detail.component";
import {EnrollmentOverviewComponent} from "./enrollment/page/enrollment-content/enrollment-overview/enrollment-overview.component";
import {
  EnrollmentLessonsComponent
} from "./enrollment/page/enrollment-content/enrollment-lessons/enrollment-lessons.component";
import {EnrollmentPostsComponent} from "./enrollment/page/enrollment-content/enrollment-posts/enrollment-posts.component";
import {LessonDetailComponent} from "./enrollment/page/enrollment-content/lesson-detail/lesson-detail.component";
import {PostListComponent} from "./administration/courses/page/manage-post/post-list/post-list.component";
import {PostAddComponent} from "./administration/courses/page/manage-post/post-add/post-add.component";
import {PostDetailComponent} from "./administration/courses/page/manage-post/post-detail/post-detail.component";
import {EditPostComponent} from "./administration/courses/page/manage-post/post-edit/post-edit.component";
import {PostTrashComponent} from "./administration/courses/page/manage-post/post-trash/post-trash.component";
import {ManageQuizComponent} from "./administration/courses/page/manage-quiz/manage-quiz.component";
import {AddQuizComponent} from "./administration/courses/page/manage-quiz/add-quiz/add-quiz.component";
import {EditQuizComponent} from "./administration/courses/page/manage-quiz/edit-quiz/edit-quiz.component";
import {AddQuestionComponent} from "./administration/courses/page/manage-quiz/add-question/add-question.component";
import {EditQuestionComponent} from "./administration/courses/page/manage-quiz/edit-question/edit-question.component";
import {QuizTrashComponent} from "./administration/courses/page/manage-quiz/quiz-trash/quiz-trash.component";
import {EnrollmentStatisticsComponent} from "./administration/statistics/page/enrollment-statistics.component";
import {
  EnrollmentStatisticDetailComponent
} from "./administration/statistics/page/enrollment-statistic-detail/enrollment-statistic-detail.component";
import {RoleGuard} from "./role.guard";
import {ListTeachersComponent} from "./administration/teachers/page/list-teachers/list-teachers.component";
import {TeacherDetailComponent} from "./administration/teachers/page/teacher-detail/teacher-detail.component";
import {SalaryTeacherComponent} from "./administration/teachers/page/salary-teacher/salary-teacher.component";
import {QuizSubmitComponent} from "./enrollment/page/enrollment-content/quiz-submit/quiz-submit.component";
import {UsageComponent} from "./usage/usage.component";
import {AboutUnpublishComponent} from "./usage/about-unpublish/about-unpublish.component";
import {ErrorComponent} from "./error/error.component";
import {ChangeCourseComponent} from "./browse-course/page/change-course/change-course.component";
import {
  AddCoursePathComponent
} from "./administration/course-path/page/add-course-path/add-course-path.component";
import {ListCoursePathComponent} from "./administration/course-path/page/list-course-path/list-course-path.component";
import {
  AddCourseToPathComponent
} from "./administration/course-path/page/add-course-to-path/add-course-to-path.component";
import {
  CoursePathTrashComponent
} from "./administration/course-path/page/course-path-trash/course-path-trash.component";
import {EditCoursePathComponent} from "./administration/course-path/page/edit-course-path/edit-course-path.component";
export const routes: Routes = [
  {
    title: 'Welcome to E learning!',
    path: '',
    component: BrowseCoursesComponent,
  },
  {
    title: 'Usage',
    path: 'usage',
    component: UsageComponent,
    children: [
      {
        title: 'Usage about unpublishing course',
        path: 'about-unpublish',
        component: AboutUnpublishComponent,
        canActivate: [RoleGuard],
        data: {
          requiredRoles: ['ROLE_admin', 'ROLE_teacher'],
          errorStatus: 403,
          errorMessage: 'Only admin and teacher can access usage page'
        },
      }
    ]
  },
  {
    title: 'Course detail',
    path: 'courses/:id',
    component: BrowseCourseDetailComponent
  },
  {
    title: 'Change course',
    path: 'change-course',
    component: ChangeCourseComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
    }
  },
  {
    title: 'Course content',
    path: 'enrollments/:id',
    component: EnrollmentContentComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user']
    },
    children: [
      {
        title: 'Enrollment overview',
        path: 'overview',
        component: EnrollmentOverviewComponent
      },
      {
        title: 'Enrollment lessons',
        path: 'lessons',
        component: EnrollmentLessonsComponent
      },
      {
        title: 'Enrollment posts',
        path: 'posts',
        component: EnrollmentPostsComponent
      },
      {
        title: 'Submit quiz page',
        path: 'quiz-submit/:quizId',
        component: QuizSubmitComponent
      }
    ]
  },
  {
    title: 'Lesson detail',
    path: 'enrollments/:enrollmentId/lessons/:lessonId',
    component: LessonDetailComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user']
    },
  },
  {
    title: 'Checkout course',
    path: 'checkout/:courseId',
    component: CheckoutComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
      deniedRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only user can access checkout page'
    }
  },
  {
    title: 'Checkout payment',
    path: 'checkout/pay/:orderId',
    component: PaymentComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
      deniedRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only user can access payment page'
    }
  },
  {
    title: 'My Orders',
    path: 'my-orders',
    component: MyOrdersComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
      deniedRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only user can access my order page'
    }
  },
  {
    title: 'My order detail',
    path: 'my-orders/:orderId',
    component: MyOrderDetailComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
      deniedRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only user can access my order detail page'
    }
  },
  {
    title: 'Welcome to my courses!',
    path: 'my-enrollments',
    component: MyEnrollmentsComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_user'],
      deniedRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only user can access my enrollments page'
    }
  },
  {
    title: 'Welcome to dashboard!',
    path: 'administration',
    component: DashboardComponent,
    canActivate: [RoleGuard],
    data: {
      requiredRoles: ['ROLE_admin', 'ROLE_teacher'],
      errorStatus: 403,
      errorMessage: 'Only admin and teacher can access administration dashboard page'
    },
    children: [
      // Course path management
      {
        title: 'List Course path',
        path: 'course-paths',
        component: ListCoursePathComponent,
      },
      {
        title: 'List Course path in trash',
        path: 'course-paths/trash',
        component: CoursePathTrashComponent,
      },
      {
        title: 'Add Course to Path',
        path: 'course-paths/add-course/:coursePathId',
        component: AddCourseToPathComponent
      },
      {
        title: 'Create Course Path',
        path: 'course-paths/add',
        component: AddCoursePathComponent,
      },
      {
        title: 'Create Course Path',
        path: 'course-paths/edit/:coursePathId',
        component: EditCoursePathComponent,
      },
      // Teacher management
      {
        title: 'Teacher management center!',
        path: 'teachers',
        component: ListTeachersComponent,
        canActivate: [RoleGuard],
        data: {
          requiredRoles: ['ROLE_admin'],
          errorStatus: 403,
          errorMessage: 'Only admin can access teachers management page'
        },
      },
      {
        title: 'Teacher detail',
        path: 'teachers/:teacher',
        component: TeacherDetailComponent
      },
      {
        title: 'Teacher salary',
        path: 'teachers/:teacher/salary',
        component: SalaryTeacherComponent,
        canActivate: [RoleGuard],
        data: {
          requiredRoles: ['ROLE_admin'],
          errorStatus: 403,
          errorMessage: 'Only admin can access teacher salary page'
        },
      },
      // Course management
      {
        title: 'Course management center!',
        path: 'courses',
        component: ListCourseComponent
      },
      {
        title: "Course trash",
        path: 'courses/trash',
        component: CourseTrashComponent
      },
      {
        title: "Create new course",
        path: 'courses/add',
        component: AddCourseComponent
      },
      {
        title: 'Edit course',
        path: 'courses/edit/:id',
        component: EditCourseComponent
      },
      {
        title: 'Assign course to teacher',
        path: 'courses/assign-teacher/:courseId',
        component: AssignTeacherComponent
      },
      {
        title: 'Set price to course',
        path: 'courses/set-price/:courseId',
        component: SetPriceComponent,
      },
      {
        title: 'Course detail management center!',
        path: 'courses/:id',
        component: CourseDetailComponent
      },
      {
        title: 'Add Section',
        path: 'courses/:id/sections/add',
        component: AddSectionComponent
      },
      {
        title: 'Edit section',
        path: 'courses/:id/sections/edit/:sectionId',
        component: EditSectionComponent
      },
      {
        title: "Add Lesson",
        path: 'courses/:courseId/sections/:sectionId/lessons/add',
        component: AddLessonComponent
      },
      {
        title: "Edit Lesson",
        path: 'courses/:courseId/sections/:sectionId/lessons/edit/:lessonId',
        component: EditLessonComponent
      },
      // Manage Quiz
      {
        title: "Manage Quiz",
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes',
        component: ManageQuizComponent
      },
      {
        title: "Quiz Trash",
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes/trash',
        component: QuizTrashComponent
      },
      {
        title: 'Add Quiz',
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes/add',
        component: AddQuizComponent
      },
      {
        title: 'Edit Quiz',
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes/edit/:quizId',
        component: EditQuizComponent
      },
      {
        title: 'Add Question',
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes/add-question/:quizId',
        component: AddQuestionComponent
      },
      {
        title: 'Edit Question',
        path: 'courses/:courseId/sections/:sectionId/lessons/:lessonId/quizzes/:quizId/edit-question/:questionId',
        component: EditQuestionComponent
      },
      {
        title: 'Request course list',
        path: 'courses/:courseId/requests',
        component: RequestListComponent
      },
      {
        title:'Post list',
        path:'courses/:courseId/posts',
        component: PostListComponent
      },
      {
        title:'Post trash',
        path: 'courses/:courseId/posts/trash',
        component: PostTrashComponent
      },
      {
        title:'Post add',
        path:'courses/:courseId/posts/add',
        component: PostAddComponent
      },
      {
        title:'Post detail',
        path:'courses/:courseId/posts/:postId',
        component: PostDetailComponent
      },
      {
        title:'Post edit',
        path:'courses/:courseId/posts/:postId/edit',
        component: EditPostComponent
      },
      {
        title: 'Request course detail',
        path: 'courses/:courseId/requests/:requestId',
        component: RequestCourseDetailComponent
      },
      {
        title: 'Request publish course',
        path: 'courses/request-publish/:courseId',
        component: RequestCourseComponent,
        data: {
          requestType: 'publish'
        }
      },
      {
        title: 'Request unpublish course',
        path: 'courses/request-unpublish/:courseId',
        component: RequestCourseComponent,
        data: {
          requestType: 'unpublish'
        }
      },
      {
        title: 'Approve course request',
        path: 'courses/:courseId/requests/approve/:requestId',
        component: ResolveRequestComponent,
        data: {
          resolveType: 'Approve'
        }
      },
      {
        title: 'Reject course request',
        path: 'courses/:courseId/requests/reject/:requestId',
        component: ResolveRequestComponent,
        data: {
          resolveType: 'Reject'
        }
      },

      // Student management
      {
        title: 'Enrollment statistics',
        path: 'enrollment-statistics',
        component: EnrollmentStatisticsComponent,
      },
      // Link posts
      {
        title: 'Enrollment statistics posts',
        path: 'enrollment-statistics/posts',
        component: EnrollmentPostsComponent
      },
      {
        title: 'Enrollment statistics detail',
        path: 'enrollment-statistics/:courseId',
        component: EnrollmentStatisticDetailComponent
      },

      // Discount management
      {
        title: 'Discount management center!',
        path: 'discounts',
        component: ListDiscountComponent
      },
      {
        title: 'Discount trash',
        path: 'discounts/trash',
        component: DiscountTrashComponent
      },
      {
        title: 'Create new discount',
        path: 'discounts/add',
        component: AddDiscountComponent
      },
      {
        title: 'Edit discount',
        path: 'discounts/edit/:id',
        component: EditDiscountComponent
      }
    ]
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: 'Error page'
  },
  {
    path: '**',
    component: ErrorComponent,
    title: 'Page not found',
  }
];
