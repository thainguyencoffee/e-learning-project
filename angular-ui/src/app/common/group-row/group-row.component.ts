import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-group-row',
  standalone: true,
  imports: [],
  template: `
    <ng-container>
      <div *ngFor="let item of controlArray.controls; let i = index">
        
      </div>
    </ng-container>
  `,
  // templateUrl: './group-row.component.html',
  styleUrl: './group-row.component.css'
})
export class GroupRowComponent implements OnInit {

  @Input({required: true})
  group?: FormGroup;

  @Input({required: true})
  field = ''

  @Input({required: true})
  label = '';

  controlArray?: FormArray;

  createItem?: () => FormGroup;
  
  ngOnInit(): void {
    this.controlArray = this.group!.get(this.field) as FormArray;
  }

  addItem(): void {
    this.controlArray?.push(this.createItem);
  }

  removeItem(index: number) {
    this.controlArray?.removeAt(index);
  }

  
}
