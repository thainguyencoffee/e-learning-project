export interface ExchangeDetails {
  enrollmentId?: number,
  courseId?: number,
  additionalPrice?: string
}

export enum OrderType {
  PURCHASE = 'PURCHASE',
  EXCHANGE = 'EXCHANGE'
}

export interface Order {
  id: string;
  orderDate: string;
  totalPrice: string;
  discountedPrice: string;
  discountCode: string;
  status: string;
  orderType: OrderType,
  exchangeDetails?: ExchangeDetails,
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}
