import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subscription, interval} from 'rxjs';
import {reverseProxyUri} from "../../app.config";

interface UserInfoDto {
  username: string;
  firstName: string,
  lastName: string,
  email: string;
  roles: string[]
  exp: number;
}

export class User {
  static readonly ANONYMOUS = new User("", "", "", "", []);

  constructor(
    readonly username: string,
    readonly firstName: string,
    readonly lastName: string,
    readonly email: string,
    readonly roles: string[]
  ) {
  }

  get isAuthenticated(): boolean {
    return !!this.username;
  }

  hasAnyRole(...roles: string[]): boolean {
    for (const r of roles) {
      if (this.roles.includes(r)) {
        return true;
      }
    }
    return false;
  }
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private user$ = new BehaviorSubject<User>(User.ANONYMOUS);
  private refreshSub?: Subscription;

  constructor(private http: HttpClient) {
    this.refresh();
  }

  refresh(): void {
    this.refreshSub?.unsubscribe();
    this.http.get(reverseProxyUri + '/bff/me').subscribe({
      next: (dto: any) => {
        const user = dto as UserInfoDto;
        if (
          user.username !== this.user$.value.username ||
          user.firstName !== this.user$.value.firstName ||
          user.lastName !== this.user$.value.lastName ||
          user.email !== this.user$.value.email ||
          (user.roles || []).toString() !== this.user$.value.roles.toString()
        ) {
          this.user$.next(
            user.username
              ? new User(
                user.username || '',
                user.firstName || '',
                user.lastName || '',
                user.email || '',
                user.roles || []
              )
              : User.ANONYMOUS
          );
        }
        if (!!user.exp) {
          const now = Date.now();
          const delay = (1000 * user.exp - now) * 0.8;
          if (delay > 2000) {
            this.refreshSub = interval(delay).subscribe(() => this.refresh());
          }
        }
      },
      error: (error) => {
        console.warn(error);
        this.user$.next(User.ANONYMOUS);
      },
    });
  }

  get valueChanges(): Observable<User> {
    return this.user$;
  }

  get current(): User {
    return this.user$.value;
  }
}
