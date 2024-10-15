import {ApplicationConfig, importProvidersFrom} from '@angular/core';
import {ExtraOptions, RouterModule} from '@angular/router';

import {provideHttpClient} from '@angular/common/http';
import { routes } from './app.routes';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

const routeConfig: ExtraOptions = {
  onSameUrlNavigation: 'reload',
  scrollPositionRestoration: 'enabled'
};

export const appConfig: ApplicationConfig = {
    providers: [
      importProvidersFrom(RouterModule.forRoot(routes, routeConfig), BrowserAnimationsModule),
      provideHttpClient()],
};

export const reverseProxyUri = 'http://ubuntu:7080';
export const baseUri = `${reverseProxyUri}/angular-ui`;
