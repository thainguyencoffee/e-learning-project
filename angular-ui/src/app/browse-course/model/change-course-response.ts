export enum Type {
  BASIC_CHANGE = 'BASIC_CHANGE',
  PENDING_PAYMENT_ADDITIONAL = 'PENDING_PAYMENT_ADDITIONAL',
}

export enum Status {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  PENDING = 'PENDING'
}

export interface ChangeCourseResponse {
  orderId?: string,
  type: Type,
  status: Status,
  priceAdditional?: string
}
