import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {UserService} from './user.service';
import {LoginComponent} from './login.component';
import {LogoutComponent} from './logout.component';

@Component({
  selector: 'app-authentication',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoginComponent, LogoutComponent, LoginComponent],
  template: `
    <span>
      <app-login *ngIf="!isAuthenticated"></app-login>
      <app-logout *ngIf="isAuthenticated"></app-logout>
    </span>
  `,
  styles: ``,
})
export class AuthenticationComponent {
  constructor(private user: UserService) {
  }

  get isAuthenticated(): boolean {
    return this.user.current.isAuthenticated;
  }
}
