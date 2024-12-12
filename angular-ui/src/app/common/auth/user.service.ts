import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subscription, interval} from 'rxjs';
import {environment} from "../../../environments/environment";

interface UserInfoDto {
  username: string;
  email: string;
  roles: string[]
  exp: number;
}

export class User {
  static readonly ANONYMOUS = new User("", "", []);

  constructor(
    readonly name: string,
    readonly email: string,
    readonly roles: string[]
  ) {
  }

  get isAuthenticated(): boolean {
    return !!this.name;
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
  resourcePath = environment.apiPath + '/api/me'

  private user$ = new BehaviorSubject<User>(User.ANONYMOUS);
  private refreshSub?: Subscription;

  constructor(private http: HttpClient) {
    this.refresh();
  }

  refresh(): void {
    this.refreshSub?.unsubscribe();
    this.http.get(this.resourcePath).subscribe({
      next: (dto: any) => {
        const user = dto as UserInfoDto;
        if (
          // Compare user fetched from server with current user
          user.username !== this.user$.value.name ||
          user.email !== this.user$.value.email ||
          (user.roles || []).toString() !== this.user$.value.roles.toString()
        ) {
          // update current user
          this.user$.next(
            user.username
              ? new User(
                user.username || '',
                user.email || '',
                user.roles || []
              )
              : User.ANONYMOUS
          );
        }
        if (!!user.exp) {
          // Calculate delay
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

  getUser() {
    return this.http.get<UserInfoDto>(this.resourcePath)
  }

  get valueChanges(): Observable<User> {
    return this.user$;
  }

  get current(): User {
    return this.user$.value;
  }
}
