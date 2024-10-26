import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PaymentRequestDto} from "./payment-request.dto";
import {Payment} from "./payment/payment";

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  http = inject(HttpClient);
  resourcePath = '/bff/api/payments'

  getAllPaymentsByOrder(orderId: string) {
    const url = `${this.resourcePath}/orders/${orderId}`;
    return this.http.get<Payment[]>(url);
  }

  createPayment(data: PaymentRequestDto) {
    return this.http.post<Payment>(this.resourcePath, data);
  }

}
