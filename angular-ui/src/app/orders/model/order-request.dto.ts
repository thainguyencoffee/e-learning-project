class OrderItemDto {
  id?: number

  constructor(data: Partial<OrderItemDto>) { Object.assign(this, data); }

}

export class OrderRequestDto {
  items?: OrderItemDto[];
  discountCode?: string;

  constructor(data: Partial<OrderRequestDto>) { Object.assign(this, data); }
}
