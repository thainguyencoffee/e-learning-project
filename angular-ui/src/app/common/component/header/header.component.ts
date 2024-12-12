import { Component } from '@angular/core';
import {RouterLink} from "@angular/router";
import {UserService} from "../../auth/user.service";
import {LoginComponent} from "../../auth/login.component";
import {LogoutComponent} from "../../auth/logout.component";
import {NgIf} from "@angular/common";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink,
    LoginComponent,
    LogoutComponent,
    NgIf
  ],
  templateUrl: './header.component.html',
})
export class HeaderComponent {

  constructor(private userService: UserService) {
  }

  get isAuthenticated(): boolean {
    return this.userService.current.isAuthenticated;
  }

  get isTeacher(): boolean {
    return this.userService.current.hasAnyRole('ROLE_teacher');
  }

  get isAdmin(): boolean {
    return this.userService.current.hasAnyRole('ROLE_admin');
  }

  get adminConsoleUrl() {
    return environment.keycloakPath + '/admin/keycloak101/console/';
  }

  get accountManagementUrl() {
    return environment.keycloakPath + '/realms/keycloak101/account/';
  }

}
