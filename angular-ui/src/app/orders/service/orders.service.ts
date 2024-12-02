import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {OrderRequestDto} from "../model/order-request.dto";
import {Order} from "../model/order";
import {PageWrapper} from "../../common/dto/page-wrapper";


@Injectable({
  providedIn: 'root'
})
export class OrdersService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/orders'

  createOrder(data: OrderRequestDto) {
    return this.http.post<Order>(this.resourcePath, data);
  }

  getAllOrders(pageNumber: number = 0, pageSize: number = 10) {
    const url = `${this.resourcePath}?page=${pageNumber}&size=${pageSize}`;
    return this.http.get<PageWrapper<Order>>(url)
  }

  getOrder(orderId: string) {
    return this.http.get<Order>(`${this.resourcePath}/${orderId}`);
  }

}
