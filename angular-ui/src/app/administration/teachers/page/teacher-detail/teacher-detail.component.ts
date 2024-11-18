import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {MonthStats, RatingMonthStats, TeacherDetailDto} from "../../model/teacher-detail.dto";
import {Subscription} from "rxjs";
import {TeacherService} from "../../service/teacher.service";
import {Chart, registerables} from "chart.js";
import {UserService} from "../../../../common/auth/user.service";
import {NgForOf} from "@angular/common";
import {FormsModule} from "@angular/forms";

Chart.register(...registerables);

@Component({
  selector: 'app-teacher-detail',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    FormsModule,
  ],
  templateUrl: './teacher-detail.component.html',
  styleUrl: './teacher-detail.component.css'
})
export class TeacherDetailComponent implements OnInit, OnDestroy {

  route = inject(ActivatedRoute)
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  teacherService = inject(TeacherService);
  userService = inject(UserService);

  teacher?: string;
  teacherDetailDto?: TeacherDetailDto;
  currentYear = new Date().getFullYear();
  selectedYear?: number;
  navigationSubscription?: Subscription;

  coursesByMonthChart: any;
  draftCoursesByMonthChart: any;
  studentsEnrolledByMonthChart: any;
  ratingOverallByMonthChart: any;

  ngOnInit(): void {
    this.loadData(0);

    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData(0);
      }
    })
  }

  private loadData(pageNumber: number) {
    this.teacher = this.route.snapshot.params['teacher'];
    this.selectedYear = this.route.snapshot.queryParams['year'] || this.currentYear;

    this.teacherService.getTeacherDetail(this.teacher!, this.selectedYear, pageNumber)
      .subscribe({
        next: data => {
          this.teacherDetailDto = data;
          this.initializeCharts();
        },
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  ngOnDestroy(): void {
    this.navigationSubscription!.unsubscribe();
  }

  private fillMissingMonths(stats: MonthStats[]): MonthStats[] {
    const currentMonth = new Date().getMonth() + 1; // Lấy tháng hiện tại (1-12)

    const validMonths: MonthStats[] = Array.from({length: currentMonth}, (_, i) => ({
      month: i + 1,
      count: 0,
    }));

    stats.forEach((stat) => {
      if (stat.month <= currentMonth) {
        validMonths[stat.month - 1].count = stat.count;
      }
    });

    return validMonths;
  }

  private fillMissingMonthsRating(stats: RatingMonthStats[]): RatingMonthStats[] {
    const currentMonth = new Date().getMonth() + 1; // Lấy tháng hiện tại (1-12)

    const validMonths: RatingMonthStats[] = Array.from({length: currentMonth}, (_, i) => ({
      month: i + 1,
      rating: 0,
    }));

    stats.forEach((stat) => {
      if (stat.month <= currentMonth) {
        validMonths[stat.month - 1].rating = stat.rating;
      }
    });

    return validMonths;
  }


  private initializeCharts() {
    if (!this.teacherDetailDto) return;

    const stats = this.teacherDetailDto.statistics;

    // Chuẩn hóa dữ liệu
    const coursesByMonthData = this.fillMissingMonths(stats.coursesByMonth);
    const draftCoursesByMonthData = this.fillMissingMonths(stats.draftCoursesByMonth);
    const studentsEnrolledByMonthData = this.fillMissingMonths(stats.studentsEnrolledByMonth);
    const ratingOverallByMonthData = this.fillMissingMonthsRating(stats.ratingOverallByMonth);

    // Hủy các chart cũ trước khi tạo mới
    if (this.coursesByMonthChart) {
      this.coursesByMonthChart.destroy();
    }
    if (this.draftCoursesByMonthChart) {
      this.draftCoursesByMonthChart.destroy();
    }
    if (this.studentsEnrolledByMonthChart) {
      this.studentsEnrolledByMonthChart.destroy();
    }
    if (this.ratingOverallByMonthChart) {
      this.ratingOverallByMonthChart.destroy();
    }

    // Biểu đồ Courses By Month
    this.coursesByMonthChart = new Chart('coursesByMonthCanvas', {
      type: 'bar',
      data: {
        labels: coursesByMonthData.map(stat => `${this.getMonthName(stat.month)}`),
        datasets: [{
          label: 'Courses Created',
          data: coursesByMonthData.map(stat => stat.count),
          backgroundColor: 'rgba(75, 192, 192, 0.2)',
          borderColor: 'rgba(75, 192, 192, 1)',
          borderWidth: 1,
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
        },
      }
    });

    // Biểu đồ Draft Courses By Month
    this.draftCoursesByMonthChart = new Chart('draftCoursesByMonthCanvas', {
      type: 'bar',
      data: {
        labels: draftCoursesByMonthData.map(stat => `${this.getMonthName(stat.month)}`),
        datasets: [{
          label: 'Draft Courses',
          data: draftCoursesByMonthData.map(stat => stat.count),
          backgroundColor: 'rgba(153, 102, 255, 0.2)',
          borderColor: 'rgba(153, 102, 255, 1)',
          borderWidth: 1,
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
        },
      }
    });

    // Biểu đồ Students Enrolled By Month
    this.studentsEnrolledByMonthChart = new Chart('studentsEnrolledByMonthCanvas', {
      type: 'line',
      data: {
        labels: studentsEnrolledByMonthData.map(stat => `${this.getMonthName(stat.month)}`),
        datasets: [{
          label: 'Students Enrolled',
          data: studentsEnrolledByMonthData.map(stat => stat.count),
          backgroundColor: 'rgba(255, 99, 132, 0.2)',
          borderColor: 'rgba(255, 99, 132, 1)',
          borderWidth: 1,
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
        },
      }
    });

    // Biểu đồ Rating Overall By Month
    this.ratingOverallByMonthChart = new Chart('ratingOverallByMonthCanvas', {
      type: 'line',
      data: {
        labels: ratingOverallByMonthData.map(stat => `${this.getMonthName(stat.month)}`),
        datasets: [{
          label: 'Rating Overall',
          data: ratingOverallByMonthData.map(stat => stat.rating),
          backgroundColor: 'rgba(255, 206, 86, 0.2)',
          borderColor: 'rgba(255, 206, 86, 1)',
          borderWidth: 1,
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
        },
      }
    });

  }

  private getMonthName(month: number): string {
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    return monthNames[month - 1];
  }

}
