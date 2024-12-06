import {Component, Input} from '@angular/core';
import {CourseWithoutSections} from "../../../browse-course/model/course-without-sections";
import {getStarsIcon} from "../../../browse-course/star-util";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-course-card',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    RouterLink,
    NgClass
  ],
  templateUrl: './course-card.component.html',
  styleUrl: './course-card.component.css'
})
export class CourseCardComponent {

  @Input({required: true}) course?: CourseWithoutSections;
  @Input({required: true}) showCheckoutButton = true;
  @Input({required: false}) showReview = true;

  @Input() customButtonText?: string;
  @Input({required: false}) disableCustomButton = false;
  @Input() customButtonAction?: (course: CourseWithoutSections) => void;

  // fallback btn
  @Input() enableFallbackButton = false;
  @Input() fallbackButtonText?: string;
  @Input({required: false}) disableFallbackButton = false;
  @Input() fallbackButtonAction?: (course: CourseWithoutSections) => void;

  protected readonly getStarsIcon = getStarsIcon;
}
