import {inject, Injectable} from "@angular/core";
import {Router} from "@angular/router";
import {FormGroup, ValidationErrors} from "@angular/forms";
import {loginOptions} from "./auth/login.component";
import {HttpClient} from "@angular/common/http";
import {baseUri} from "../app.config";

@Injectable({
  providedIn: 'root'
})
export class ErrorHandler {
  router = inject(Router);
  http = inject(HttpClient);

  handleServerError(error: ErrorResponse, group?: FormGroup, getMessage?: (key: string) => string) {
    const errorStatus = error?.status || '503';
    const errorMessage = error?.message || '';

    if (error && errorStatus === 401) {
      this.handleUnauthenticated();
      return;
    }

    if (!error || !error.fieldErrors) {
      this.router.navigate(['/error'], {
        state: {
          errorStatus: errorStatus,
          errorMessage: errorMessage
        }
      });
      return;
    }

    const errorsMap: Record<string, ValidationErrors> = {}
    for (const fieldError of error.fieldErrors) {
      const fieldName = fieldError.property;
      if (!errorsMap[fieldName]) {
        errorsMap[fieldName] = {}
      }
      let errorMessage = getGlobalErrorMessage(fieldError.code) || fieldError.message;
      if (getMessage) {
        errorMessage = getMessage(fieldError.property + '.' + fieldError.code) ||
          getMessage(fieldError.code) || errorMessage;
      }
      errorsMap[fieldName][fieldError.code] = errorMessage;
    }
    // write errors to fields
    for (const [key, value] of Object.entries(errorsMap)) {
      group?.get(key)?.setErrors(value);
    }
  }

  private handleUnauthenticated() {
    loginOptions(this.http).subscribe({
      next: opts => {
        if (opts.length) {
          const loginUri = opts[0].loginUri;
          const url = new URL(loginUri);
          url.searchParams.append('post_login_success_uri', `${baseUri}${this.router.url}`);
          url.searchParams.append('post_login_failure_uri', `${baseUri}/login-error`);
          window.location.href = url.toString();
        } else {
          throw new Error('No login options available');
        }
      },
      error: error => {
        this.router.navigate(['/error'], {
          state: {
            errorStatus: '503',
            errorMessage: 'Service is unavailable'
          }
        })
      }
    })
  }


}

export function getGlobalErrorMessage(key: string, details?: any) {
  let globalErrorMessage: Record<string, string> = {
    required: 'Please provide a value.',
    maxlength: 'Your value too long.',
    minlength: 'Your value too short.',
    validDouble: 'Please provide a valid floating point number.',
    validJson: 'Please follow the JSON format. Example array format: ["value1", "value2"]',
    REQUIRED_NOT_NULL: 'Please provide a value.',
    REQUIRED_NOT_BLANK: 'Please provide a value.',
  }
  if (details && details.requiredPattern && details.requiredPattern === "/^\\S*$/") {
    globalErrorMessage['pattern'] = 'Your value must not contain any whitespace';
  }
  return globalErrorMessage[key];
}

interface FieldError {

  code: string;
  property: string;
  message: string;
  rejectedValue: any | null;
  path: string | null;

}

interface ErrorResponse {

  status: number;
  code: string;
  message: string;
  fieldErrors?: FieldError[];

}

export function createErrNotFoundByProperty(propertyName: string, message: string): ErrorResponse {
  return {
    status: 404,
    code: 'NOT_FOUND',
    message: message,
    fieldErrors: [
      {
        code: 'NOT_FOUND',
        property: propertyName,
        message: message,
        rejectedValue: null,
        path: null
      }
    ]
  }
}


export function createErr(status: number, message: string): ErrorResponse {
  return {
    status: status,
    code: 'ERROR',
    message: message
  }
}
