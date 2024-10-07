import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../core/auth/auth.service';
import {AsyncPipe, NgIf} from '@angular/common';
import {IfAuthenticatedDirective} from '../../core/auth/if-authenticated.directive';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    NgIf,
    IfAuthenticatedDirective,
    AsyncPipe
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent{

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }

  login() {
    this.authService.login();
  }
}
