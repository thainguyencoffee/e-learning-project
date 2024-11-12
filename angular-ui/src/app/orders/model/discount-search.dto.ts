export interface DiscountSearchDto {
    code: string;
    type: string;
    percentage?: number;
    maxValue?: string;
    fixedPrice?: string;
    startDate: string;
    endDate: string;
    discountPrice: string;
}
