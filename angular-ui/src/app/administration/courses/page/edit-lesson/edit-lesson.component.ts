import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {ErrorHandler} from "../../../../common/error-handler.injectable";
import {CourseService} from "../../service/course.service";
import {updateForm} from "../../../../common/utils";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {LessonDto} from "../../model/lesson-dto";
import {InputRowComponent} from "../../../../common/input-row/input-row.component";

@Component({
  selector: 'app-edit-lesson',
  standalone: true,
  imports: [
    FormsModule,
    InputRowComponent,
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './edit-lesson.component.html',
})
export class EditLessonComponent implements OnInit {

  route = inject(ActivatedRoute)
  router = inject(Router);
  errorHandler = inject(ErrorHandler)
  courseService = inject(CourseService);

  courseId?: number;
  sectionId?: number;
  lessonId?: number;

  lessonTypesMap: Record<string, string> = {
    VIDEO: 'Video',
    TEXT: 'Documentation'
  }

  typeOfInputFile: string = ''

  editForm = new FormGroup({
    id: new FormControl({value: null, disabled: true}),
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)]),
    type: new FormControl(null, [Validators.required]),
    link: new FormControl(null, [Validators.required])
  })

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: `Lesson was updated successfully.`
    }
    return messages[key];
  }

  ngOnInit(): void {
    this.courseId = +this.route.snapshot.params['courseId'];
    this.sectionId = +this.route.snapshot.params['sectionId'];
    this.lessonId = +this.route.snapshot.params['lessonId'];

    this.courseService.getCourse(this.courseId)
      .subscribe({
        next: ({sections}) => {
          const section = sections!.find(sec => sec.id === this.sectionId);
          const lesson = section!.lessons.find(les => les.id === this.lessonId);
          this.lessonTypeChange(lesson!.type);

          updateForm(this.editForm, lesson);
        },
        error: (error) => this.errorHandler.handleServerError(error.error)
      })
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }

    const data = new LessonDto(this.editForm.value);
    this.courseService.updateLesson(this.courseId!, this.sectionId!, this.lessonId!, data)
      .subscribe({
        next: () => this.router.navigate(['/administration/courses', this.courseId], {
          state: {
            msgSuccess: this.getMessage('updated')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
      });

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
