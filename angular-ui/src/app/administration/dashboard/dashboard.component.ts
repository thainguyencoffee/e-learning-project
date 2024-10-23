import {Component, inject} from '@angular/core';
import {RouterLink, RouterOutlet} from "@angular/router";
import {UserService} from "../../common/auth/user.service";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    RouterOutlet,
    NgIf
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {
  userService = inject(UserService)

  isAdmin() {
    return this.userService.current.hasAnyRole('ROLE_admin')
  }

}
