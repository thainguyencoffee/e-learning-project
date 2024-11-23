export class PaymentRequestDto {
  orderId?: string | null;
  price?: string | null;
  paymentMethod?: string | null;
  token?: string | null;

  constructor(data: Partial<PaymentRequestDto>) { Object.assign(this, data); }
}
