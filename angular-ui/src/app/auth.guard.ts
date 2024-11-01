import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {UserService} from "./common/auth/user.service";

export const authGuard: CanActivateFn = (route, state) => {
  // check if user is logged in with role
  const userService = inject(UserService)
  const router = inject(Router);

  const currentRoute = route.routeConfig?.path;

  if (!userService.current.isAuthenticated) {
    router.navigate(['/error'], {
      state: {
        errorStatus: '401',
      }
    });
    return false;
  }

  if (currentRoute?.startsWith('administration') && !userService.current.hasAnyRole('ROLE_admin', 'ROLE_teacher')) {
    router.navigate(['/error'], {
      state: {
        errorStatus: '403',
      }
    });
    return false;
  }

  return true;

};
