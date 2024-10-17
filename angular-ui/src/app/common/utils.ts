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

