import {Component, Input, OnInit} from '@angular/core';
import {AbstractControl, FormArray, FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {FieldConfiguration} from "./field-configuration";
import {InputRowComponent} from "../input-row/input-row.component";
import {InputRowFormArrayComponent} from "../input-row-2/input-row-form-array.component";

@Component({
  selector: 'app-input-object-row',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    InputRowComponent,
    InputRowFormArrayComponent
  ],
  templateUrl: './input-object-row.component.html',
})
export class InputObjectRowComponent implements OnInit {

  @Input({ required: true })
  group?: FormGroup;

  @Input({ required: true })
  field = '';

  @Input()
  inputClass = '';

  @Input({ required: true })
  label = '';

  control?: AbstractControl;

  @Input()
  addItem!: () => void;

  @Input()
  removeItem!: (index: number) => void;

  @Input() groupConfiguration: FieldConfiguration[] = [];
  @Input() fieldConfiguration?: FieldConfiguration;

  ngOnInit(): void {
    this.control = this.group?.get(this.field)!;

    if (this.control instanceof FormArray) {
      this.control.controls.forEach((item, index) => {
        if (item instanceof FormGroup) {
          if (this.groupConfiguration.length === 0) {
            throw new Error('groupConfigurations is empty.');
          }
        } else {
          if (this.fieldConfiguration === undefined) {
            throw new Error('fieldConfigurations is undefined.');
          }
        }
      });
    }
  }

  get formArray(): FormArray | null {
    return this.control instanceof FormArray ? this.control : null;
  }

  isFormGroup(control: AbstractControl): boolean {
    return control instanceof FormGroup;
  }

  getFormGroup(control: AbstractControl): FormGroup {
    return control as FormGroup;
  }

  getFormControl(control: AbstractControl): FormControl {
    return control as FormControl;
  }

}
