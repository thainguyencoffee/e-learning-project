import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {PageWrapper} from "../../../common/dto/page-wrapper";
import {DiscountDto} from "../model/discount-dto";
import {Discount} from "../model/view/discount";
import {DiscountSearchDto} from "../../../orders/model/discount-search.dto";

@Injectable(
  {providedIn: 'root'}
)
export class DiscountService {

  http = inject(HttpClient);

  resourcePath = '/bff/api/discounts'

  getAllDiscounts(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<Discount>> {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Discount>>(url)
  }

  getDiscount(currentId: number) {
    return this.http.get<Discount>(`${this.resourcePath}/${currentId}`);
  }

  createDiscount(data: DiscountDto): Observable<Discount> {
    return this.http.post<Discount>(this.resourcePath, data);
  }

  deleteCourse(id: number) {
    return this.http.delete(`${this.resourcePath}/${id}`);
  }

  getAllDiscountsInTrash(pageNumber: number = 0, pageSize: number = 10): Observable<PageWrapper<Discount>> {
    const url = `${this.resourcePath}/trash?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Discount>>(url);
  }

  deleteDiscountForce(discount: Discount) {
    return this.http.delete<void>(this.resourcePath + '/' + discount.id, {
      params: { force: 'true' }
    });
  }

  restoreDiscount(discountId: number) {
    return this.http.post<Discount>(`${this.resourcePath}/` + discountId + '/restore', {});
  }

  updateDiscount(id: number, data: DiscountDto) {
    return this.http.put<Discount>(this.resourcePath + '/' + id, data);
  }

  getDiscountByCode(code: string, originalPrice: string) {
    return this.http.get<DiscountSearchDto>(`${this.resourcePath}/code/${code}`, {
      params: { originalPrice }
    });
  }

}
