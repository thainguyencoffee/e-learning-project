export interface Discount {
  id: number,
  code: string,
  type: string,
  percentage?: number,
  maxValue?: string,
  fixedPrice?: string,
  startDate: string,
  endDate: string,
  currentUsage: number,
  maxUsage: number,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string
}

