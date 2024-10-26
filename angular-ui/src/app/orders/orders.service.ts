import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {OrderRequestDto} from "./order-request.dto";
import {Order} from "./order";
import {PageWrapper} from "../administration/courses/model/view/page-wrapper";


@Injectable({
  providedIn: 'root'
})
export class OrdersService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/orders'

  createOrder(data: OrderRequestDto) {
    return this.http.post<Order>(this.resourcePath, data);
  }

  getAllMyOrders(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}/my-orders?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Order>>(url)
  }

  getMyOrder(orderId: string) {
    return this.http.get<Order>(`${this.resourcePath}/my-orders/${orderId}`);
  }

}
