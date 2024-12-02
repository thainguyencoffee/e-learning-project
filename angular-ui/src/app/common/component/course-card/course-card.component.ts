import {Component, Input} from '@angular/core';
import {CourseWithoutSections} from "../../../browse-course/model/course-without-sections";
import {getStarsIcon} from "../../../browse-course/star-util";
import {NgForOf, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-course-card',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    RouterLink
  ],
  templateUrl: './course-card.component.html',
  styleUrl: './course-card.component.css'
})
export class CourseCardComponent {

  @Input({required: true}) course?: CourseWithoutSections;
  @Input({required: true}) showCheckoutButton = true;

  @Input() customButtonText?: string;
  @Input() customButtonAction?: (course: CourseWithoutSections) => void;

  protected readonly getStarsIcon = getStarsIcon;
}
