import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {TeacherService} from "../../service/teacher.service";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {TeacherDto} from "../../model/teacher.dto";
import {PaginationUtils} from "../../../../common/dto/page-wrapper";
import {Subscription} from "rxjs";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-list-teachers',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf
  ],
  templateUrl: './list-teachers.component.html',
})
export class ListTeachersComponent implements OnInit, OnDestroy {

  teacherService = inject(TeacherService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  route = inject(ActivatedRoute);

  teacherDtos?: TeacherDto[];
  paginationUtils?: PaginationUtils;
  navigationSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData(0);
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  private loadData(pageNumber: number){
    this.teacherService.getTeachers(pageNumber)
      .subscribe({
        next: (pageWrapper) => {
          this.paginationUtils = new PaginationUtils(pageWrapper.page);
          this.teacherDtos = pageWrapper.content;
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
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

  getPageRange(): number[] {
    return this.paginationUtils?.getPageRange() || [];
  }

}
