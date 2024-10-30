export interface Payment {
  id: string;
  orderId: string;
  amount: string;
  status: string;
  paymentDate: string;
  paymentMethod: string;
  transactionId: string;
  receiptUrl: string;
}
