import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-add-section',
  standalone: true,
  imports: [],
  templateUrl: './add-section.component.html',
})
export class AddSectionComponent implements OnInit {

  addForm = new FormGroup({
    title: new FormControl(null, [Validators.required, Validators.minLength(10), Validators.maxLength(255)])
  })

  ngOnInit(): void {
  }

}
