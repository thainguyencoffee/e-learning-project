import {FormArray, FormControl, Validators} from "@angular/forms";
import {minFormArrayLength} from "../../../../common/utils";

export function onChangeQuestionType(
  type: FormControl,
  trueFalseAnswerControl: FormControl,
  options: FormArray) {
  if (type.value === 'TRUE_FALSE') {
    // Add required validator for trueFalseAnswer
    trueFalseAnswerControl?.setValidators([Validators.required]);
    trueFalseAnswerControl?.updateValueAndValidity();

    // Remove all options and validators
    options.clear();
    options.setValidators([]); // No validators for options
    options.updateValueAndValidity();

  } else {
    // Remove validator for trueFalseAnswer
    trueFalseAnswerControl?.setValidators([]);
    trueFalseAnswerControl?.updateValueAndValidity();

    // Add validator for options when not TRUE_FALSE
    options.setValidators([minFormArrayLength(2)]);
    options.updateValueAndValidity();
  }
}
