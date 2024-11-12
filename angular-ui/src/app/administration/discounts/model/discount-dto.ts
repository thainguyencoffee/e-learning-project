export class DiscountDto {
  code?: string | null;
  type?: string | null;
  percentage?: number | null;
  maxValue?: string | null;
  fixedPrice?: string | null;
  currency?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  maxUsage?: number | null;

  constructor(data: Partial<DiscountDto>) {
    Object.assign(this, data);
    if (this.maxValue) {
      this.maxValue = this.currency + ''+ this.maxValue;
    }
    if (this.fixedPrice) {
      this.fixedPrice = this.currency + ''+ this.fixedPrice;
    }
  }
}
