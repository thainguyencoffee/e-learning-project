export class PaymentRequestDto {
  orderId?: string | null;
  amount?: string | null;
  paymentMethod?: string | null;
  token?: string | null;

  constructor(data: Partial<PaymentRequestDto>) { Object.assign(this, data); }
}
