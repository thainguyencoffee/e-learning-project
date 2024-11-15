import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {OrdersService} from "../../orders/service/orders.service";
import {Order} from "../../orders/model/order";
import {createErr, ErrorHandler} from "../../common/error-handler.injectable";
import {NgIf} from "@angular/common";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {updateForm} from "../../common/utils";
import {InputRowComponent} from "../../common/input-row/input-row.component";
import {PaymentRequestDto} from "../model/payment-request.dto";
import {PaymentService} from "../service/payment.service";

declare var StripeCheckout: any;

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    InputRowComponent,
    ReactiveFormsModule
  ],
  templateUrl: './payment.component.html',
})
export class PaymentComponent implements OnInit {

  route = inject(ActivatedRoute);
  router = inject(Router);
  orderService = inject(OrdersService);
  paymentService = inject(PaymentService);
  errorHandler = inject(ErrorHandler);

  orderId?: string;
  order?: Order;

  stripePublicKey: string = 'pk_test_51PtPqOLcSSqgAPH93GXfm7Cr2uoyCkeC1Zj5Bu5WxPDuFxZNf8HSiJwyUy9MbYLvfuDiVNd5BYl2XbjEkk3Z9tS8003dgGR2D2';

  paymentMethodsMap: Record<string, string> = {
    // PAYPAL: 'Paypal',
    STRIPE: 'Stripe',
  }

  paymentForm = new FormGroup({
    orderId: new FormControl(null, [Validators.required]),
    amount: new FormControl(null, [Validators.required]),
    paymentMethod: new FormControl(null, [Validators.required]),
    token: new FormControl(null, [Validators.required])
  })

  ngOnInit(): void {
    this.orderId = this.route.snapshot.params['orderId'];
    this.orderService.getOrder(this.orderId!).subscribe({
      next: order => {
        this.order = order;
        updateForm(this.paymentForm, {
          orderId: order.id,
          amount: order.discountedPrice || order.totalPrice,
        })
      },
      error: error => {
        if (error.status === 404) {
          this.errorHandler.handleServerError({
            status: 403,
            message: 'You are not authorized to view this payment page.',
            code: 'NOT_OWNER'
          })
        } else {
          this.errorHandler.handleServerError(error.error)
        }
      }
    })
  }

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: `Payment was created successful.`
    };
    return messages[key];
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.paymentForm.markAllAsTouched();
    if (!this.paymentForm.valid) {
      return;
    }

    const data = new PaymentRequestDto(this.paymentForm.value);

    this.paymentService.createPayment(data)
      .subscribe({
        next: (payment) => this.router.navigate(['/my-orders', payment.orderId], {
          state: {
            msgSuccess: this.getMessage('created')
          }
        }),
        error: (error) => this.errorHandler.handleServerError(error.error, this.paymentForm, this.getMessage)
      });
  }

  openCheckout() {
    const handler = StripeCheckout.configure({
      key: this.stripePublicKey,
      locale: 'auto',
      token: (token: any) => {
        // Gửi token đến backend của bạn
        if (token.id) {
          this.paymentForm.get('token')?.setValue(token.id);
          this.handleSubmit()
        } else {
          this.errorHandler.handleServerError(createErr(503, 'Stripe handle your card error, Please try later'), this.paymentForm, this.getMessage)
        }
      }
    });

    handler.open({
      name: 'ELearning',
      description: 'Course checkout',
      image: 'https://bookstore-bucket.sgp1.digitaloceanspaces.com/assets/Thinking.jpg',
      zipCode: false
    });

  }

  paymentMethodSelected(key: string) {
    if (key === 'STRIPE') {
      this.openCheckout();
    }
  }


}
