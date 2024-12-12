import {HttpClient} from '@angular/common/http';
import {Component} from '@angular/core';
import {ReactiveFormsModule, Validators} from '@angular/forms';
import {map, Observable} from 'rxjs';
import {UserService} from './user.service';
import {Router} from '@angular/router';
import {CommonModule} from '@angular/common';
import {environment} from "../../../environments/environment";

interface LoginOptionDto {
  label: string;
  loginUri: string;
}

export function loginOptions(http: HttpClient): Observable<Array<LoginOptionDto>> {
  return http
    .get(environment.apiPath + '/login-options')
    .pipe(map((dto: any) => dto as LoginOptionDto[]));
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <button class="btn btn-light" (click)="login()" [disabled]="!isLoginEnabled">Login</button>
  `,
})
export class LoginComponent {
  private loginUri?: string;

  constructor(
    http: HttpClient,
    private user: UserService,
    private router: Router
  ) {
    loginOptions(http).subscribe((opts) => {
      if (opts.length) {
        this.loginUri = opts[0].loginUri;
      }
    });
  }

  get isLoginEnabled(): boolean {
    return !this.user.current.isAuthenticated && !!this.loginUri;
  }

  login() {
    if (!this.loginUri) {
      return;
    }

    const url = new URL(this.loginUri);
    url.searchParams.append('post_login_success_uri',`${environment.basePath}${this.router.url}`);
    url.searchParams.append('post_login_failure_uri', `${environment.basePath}/login-error`);
    window.location.href = url.toString();
  }

}
