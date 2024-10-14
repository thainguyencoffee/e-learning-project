import { Routes } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./administration/dashboard/dashboard.component";
import {ListCourseComponent} from "./administration/courses/page/list-course/list-course.component";
import {AddCourseComponent} from "./administration/courses/page/add-course/add-course.component";
import {ErrorComponent} from "./error/error.component";
import {EditCourseComponent} from "./administration/courses/page/edit-course/edit-course.component";
import {CourseDetailComponent} from "./administration/courses/page/course-detail/course-detail.component";
import { AddSectionComponent } from './administration/courses/page/add-section/add-section.component';

export const routes: Routes = [
  {
    title: 'Welcome to E learning!',
    path: '',
    component: HomeComponent
  },
  {
    title: 'Welcome to dashboard!',
    path: 'administration',
    component: DashboardComponent,
    children: [
      {
        title: 'Course management center!',
        path: 'courses',
        component: ListCourseComponent
      },
      {
        title: "Create new course",
        path: 'courses/add',
        component: AddCourseComponent
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
        title: 'Edit course',
        path: 'courses/edit/:id',
        component: EditCourseComponent
      },
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
