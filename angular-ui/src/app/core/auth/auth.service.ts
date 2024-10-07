import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, distinctUntilChanged, map, Observable} from 'rxjs';
import {User} from './user.model';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Quản lý trạng thái người dùng hiện tại
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser = this.currentUserSubject
    .asObservable()
    .pipe(distinctUntilChanged());
  public isAuthenticated = this.currentUser.pipe(map(user => !!user));

  private http = inject(HttpClient);
  private router = inject(Router);

  login(): void {
    window.open("/edge/oauth2/authorization/keycloak", "_self");
  }

  // Hàm tải thông tin người dùng hiện tại dựa trên session từ server
  loadCurrentUser(): void {
    this.http.get<User>("/edge/user").subscribe(
      (user) => {
        if (user.username) {
          this.setAuth(user);
        } else {
          this.purgeAuth()
        }
      });
  }

  logout(): void {
    const csrfToken = this.getCsrfTokenFromCookie();
    if (csrfToken) {
      const headers = new HttpHeaders({
        'X-XSRF-TOKEN': csrfToken,
        "X-POST-LOGOUT-SUCCESS-URI": `/`
      })
      this.http.post('/edge/logout', {}, {headers}).subscribe(() => {
        this.purgeAuth();
      })

    } else {
      console.error("CSRF Token not found")
    }
  }

  // Cập nhật trạng thái xác thực người dùng
  private setAuth(user: User): void {
    this.currentUserSubject.next(user); // Cập nhật thông tin người dùng
  }

  // Xóa trạng thái xác thực
  private purgeAuth(): void {
    this.currentUserSubject.next(null); // Xóa thông tin người dùng
  }

  // Hàm để lấy CSRF token từ cookie
  private getCsrfTokenFromCookie(): string | null {
    const name = 'XSRF-TOKEN=';
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
      const cookie = cookies[i].trim();
      if (cookie.indexOf(name) === 0) {
        return cookie.substring(name.length, cookie.length);
      }
    }
    return null;
  }

}
