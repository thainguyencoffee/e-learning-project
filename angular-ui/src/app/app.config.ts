import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {ExtraOptions, provideRouter, RouterModule} from '@angular/router';

import { routes } from './app.routes';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';

const routeConfig: ExtraOptions = {
  onSameUrlNavigation: 'reload',
  scrollPositionRestoration: 'enabled'
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    importProvidersFrom(RouterModule.forRoot(routes, routeConfig), BrowserAnimationsModule, HttpClientModule),
  ],
};
