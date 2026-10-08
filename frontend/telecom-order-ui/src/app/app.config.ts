import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { correlationIdInterceptor } from './core/correlation-id.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // HttpClient foundation. Live backend calls arrive in Phase 9.
    // The interceptor guarantees every outbound request carries X-Correlation-ID.
    provideHttpClient(withInterceptors([correlationIdInterceptor])),
  ],
};
