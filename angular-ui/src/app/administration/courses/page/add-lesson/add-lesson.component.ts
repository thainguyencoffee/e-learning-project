import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {CourseService} from "../../service/course.service";
import {Course} from "../../model/view/course";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {LessonDto} from "../../model/lesson-dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-add-lesson',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './add-lesson.component.html',
})
export class AddLessonComponent implements OnInit{

  route = inject(ActivatedRoute);
  router = inject(Router);
  courseService = inject(CourseService);
  errorHandler = inject(ErrorHandler)

  courseId?: number;
  sectionId?: number;
  courseDto?: Course

  typeOfInputFile: string = ''

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Lesson was added successfully.`
    }
    return messages[key];
  }

  lessonTypesMap: Record<string, string> = {
    VIDEO: 'Video',
    TEXT: 'Documentation'
  }

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)]),
    type: new FormControl(null, [Validators.required]),
    link: new FormControl(null, [Validators.required])
  })

  ngOnInit(): void {
    this.courseId = this.route.snapshot.params['courseId'];
    this.sectionId = this.route.snapshot.params['sectionId'];
    if (this.courseId){
      this.courseService.getCourse(this.courseId)
        .subscribe({
          next: data => this.courseDto = data,
          error: error =>  this.errorHandler.handleServerError(error.error)
        })
    }
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return ;
    }

    const data = new LessonDto(this.addForm.value);
    this.courseService.addLesson(this.courseId!, this.sectionId!, data)
      .subscribe({
        next:() => this.router.navigate(['/administration/courses', this.courseId], {
          state: {
            msgSuccess: this.getMessage('created')
          }
        }),
        error: error => this.errorHandler.handleServerError(error.error)
      })
  }

  lessonTypeChange(lessonTypeKey: string) {
    if (lessonTypeKey === 'VIDEO') {
      this.typeOfInputFile = 'videoFile'
    } else if (lessonTypeKey === 'TEXT') {
      this.typeOfInputFile = 'docFile'
    } else {
      this.typeOfInputFile = ''
    }
  }

}
