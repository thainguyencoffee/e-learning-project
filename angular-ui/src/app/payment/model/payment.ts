export interface Payment {
  id: string;
  orderId: string;
  price: string;
  status: string;
  paymentDate: string;
  paymentMethod: string;
  transactionId: string;
  receiptUrl: string;
  failureReason: string;
}
