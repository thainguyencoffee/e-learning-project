import {AbstractControl, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';


/**
* Update all controls of the provided form group with the given data.
*/

export function updateForm(group: FormGroup, data: any) {
  for (const field in group.controls) {
    const control = group.get(field)!;

    // Lấy giá trị từ data, nếu không có thì gán null
    let value = data[field] === undefined ? null : data[field];

    // Kiểm tra các điều kiện để xác định xem có cần chuyển đổi giá trị hay không
    const controlIsEmptyOrNotArray = !control.value || !Array.isArray(control.value);
    const valueIsObjectOrArray = typeof value === 'object' && value !== null;

    if (value && controlIsEmptyOrNotArray && valueIsObjectOrArray) {
      // Chuyển đổi giá trị thành chuỗi JSON nếu cần
      value = JSON.stringify(value, undefined, 2);
    }

    // Cập nhật giá trị cho điều khiển
    control.setValue(value);
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
