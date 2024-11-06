import {AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';


/**
* Update all controls of the provided form group with the given data.
*/

export function updateForm(group: FormGroup, data: any) {
  for (const field in group.controls) {
    const control = group.get(field)!;
    let value = data[field] === undefined ? null : data[field];
    control.setValue(value);
  }
}

export function updateFormAdvanced(group: FormGroup, data: any, createAnswerOption: () => AbstractControl) {
  for (const field in group.controls) {
    const control = group.get(field)!;
    const value = data && data[field] !== undefined ? data[field] : null;

    if (control instanceof FormGroup && value && typeof value === 'object' && !Array.isArray(value)) {
      updateFormAdvanced(control, value, createAnswerOption);
    } else if (control instanceof FormArray && Array.isArray(value)) {
      // Xóa và cập nhật lại FormArray
      control.clear();
      value.forEach((item) => {
        const itemControl = createAnswerOption();

        // Kiểm tra nếu itemControl là FormGroup, gọi đệ quy. Nếu là FormControl, đặt giá trị trực tiếp.
        if (itemControl instanceof FormGroup) {
          updateFormAdvanced(itemControl, item, createAnswerOption);
        } else if (itemControl instanceof FormControl) {
          itemControl.setValue(item);
        }
        control.push(itemControl);
      });
    } else {
      control.setValue(value);
    }
  }
}

export const validJson: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (control.value === null) {
    return null;
  }
  try {
    JSON.parse(control.value);
    return null;
  } catch (e) {
    return { validJson: { value: control.value } }
  }
};

export function calcDifference(price1: string, price2: string): string | null {
  const currency1 = price1.substring(0, 3);
  const currency2 = price2.substring(0, 3);

  if (currency1 !== currency2) {
    throw new Error('Currencies are not the same');
  }

  const numericPrice1 = parseFloat(price1.substring(3).replace(/,/g, ''));
  const numericPrice2 = parseFloat(price2.substring(3).replace(/,/g, ''));

  const difference = numericPrice1 - numericPrice2;

  return `${currency1}${difference.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function calcMultiplier(price: string, multiplier: number): string {
  const currency = price.substring(0, 3);
  const numericPrice = parseFloat(price.substring(3).replace(/,/g, ''));

  const result = numericPrice - (numericPrice * multiplier / 100);

  return `${currency}${result.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}
