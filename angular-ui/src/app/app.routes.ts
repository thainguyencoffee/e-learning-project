import { Routes } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./administration/dashboard/dashboard.component";
import {ListCourseComponent} from "./administration/courses/page/list-course/list-course.component";
import {AddCourseComponent} from "./administration/courses/page/add-course/add-course.component";
import {ErrorComponent} from "./error/error.component";

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
      }
    ]
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: 'Error page'
  },
];
