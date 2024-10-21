import {inject, Injectable} from "@angular/core";
import {Router} from "@angular/router";
import {FormGroup, ValidationErrors} from "@angular/forms";

@Injectable({
  providedIn: 'root'
})
export class ErrorHandler {
  router = inject(Router);

  handleServerError(error: ErrorResponse, group?: FormGroup, getMessage?: (key: string) => string) {
    // show general error page
    if (!error || !error.fieldErrors) {
      this.router.navigate(['/error'], {
        state:{
          errorStatus: error.status ? + '' + error.status : '503',
          errorMessage: error.message || ''
        }
      })
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
}

export function getGlobalErrorMessage(key: string, details?: any) {
  let globalErrorMessage: Record<string, string> = {
    required: 'Please provide a value.',
    maxlength: 'Your value too long.',
    minlength: 'Your value too short.',
    validDouble: 'Please provide a valid floating point number.',
    REQUIRED_NOT_NULL: 'Please provide a value.',
    REQUIRED_NOT_BLANK: 'Please provide a value.',
  }
  return globalErrorMessage[key];
}

interface FieldError {

  code: string;
  property: string;
  message: string;
  rejectedValue: any|null;
  path: string|null;

}

interface ErrorResponse {

  status: number;
  code: string;
  message: string;
  fieldErrors?: FieldError[];

}
