import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {Subscription} from "rxjs";
import {SalaryService} from "../../service/salary.service";
import {Salary} from "../../model/salary";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {DatePipe, NgIf, SlicePipe} from "@angular/common";

@Component({
  selector: 'app-salary-teacher',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    NgIf,
    SlicePipe
  ],
  templateUrl: './salary-teacher.component.html',
  styleUrl: './salary-teacher.component.css'
})
export class SalaryTeacherComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  salaryService = inject(SalaryService);

  teacher?: string;
  salary?: Salary;
  navigationSubscription?: Subscription;
  visibleRecords: number = 8;

  ngOnInit(): void {
    this.loadData();

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    })
  }

  private loadData() {
    this.teacher = this.route.snapshot.params['teacher'];

    this.salaryService.getSalaryByTeacher(this.teacher!)
      .subscribe({
        next: data => this.salary = data,
        error: error => this.errorHandler.handleServerError(error.error)

      })

  }
  showMore(): void {
    this.visibleRecords = Math.min(this.visibleRecords + 4, this.salary!.records.length);
  }

  showAll(): void {
    this.visibleRecords = this.salary!.records.length;
  }

  showLess(): void {
    this.visibleRecords = Math.max(this.visibleRecords - 4, 8);
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  someRecordNotPaid(salary: Salary) {
    return salary.records.some(record => record.status !== 'PAID');
  }
}

