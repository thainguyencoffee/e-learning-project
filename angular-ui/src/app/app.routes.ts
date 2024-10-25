import { Routes } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./administration/dashboard/dashboard.component";
import {ListCourseComponent} from "./administration/courses/page/list-course/list-course.component";
import {AddCourseComponent} from "./administration/courses/page/add-course/add-course.component";
import {ErrorComponent} from "./error/error.component";
import {EditCourseComponent} from "./administration/courses/page/edit-course/edit-course.component";
import {CourseDetailComponent} from "./administration/courses/page/course-detail/course-detail.component";
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

export const routes: Routes = [
  {
    title: 'Welcome to E learning!',
    path: '',
    component: HomeComponent,
  },
  {
    title: 'Welcome to dashboard!',
    path: 'administration',
    component: DashboardComponent,
    // canActivate: [authGuard],
    children: [
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
      {
        title: 'Request course list',
        path: 'courses/:courseId/requests',
        component: RequestListComponent
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

      // discount
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
