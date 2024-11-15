import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {Injectable} from "@angular/core";
import {UserService} from "./common/auth/user.service";
import {map, Observable} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {

  constructor(private userService: UserService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const requiredRoles = route.data['requiredRoles'] as Array<string>;
    const deniedRoles = route.data['deniedRoles'] as Array<string>;
    const errorStatus = route.data['errorStatus'] as number;
    const errorMessage = route.data['errorMessage'] as string;

    return this.userService.getUser().pipe(
      map(user => {
        if (user.username) {
          const isDenied = deniedRoles?.some(role => user.roles.includes(role));
          if (isDenied) {
            this.router.navigate(['/error'], {state: {errorStatus, errorMessage}});
            return false;
          }

          const hasAccess = requiredRoles?.some(role => user.roles.includes(role));
          if (hasAccess) {
            return true;
          }

          this.router.navigate(['/error'], {state: {errorStatus, errorMessage}});
          return false;
        } else {
          this.router.navigate(['/error'], {
            state: {
              errorStatus: 401,
              errorMessage: 'Unauthorized'
            }
          });
          return false;
        }
      }),
    );
  }
}
