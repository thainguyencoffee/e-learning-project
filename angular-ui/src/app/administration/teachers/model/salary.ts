export interface Bonus {
  type: string,
  price: string,
}

export interface SalaryRecord {
  id: number,
  bonus: Bonus,
  createdDate: string,
  paidDate: string,
  nocByMonth: number,
  nosByMonth: number,
  totalPrice: number,
  status: string,
  failureReason: string
}

export interface Salary {
  id: number,
  teacher: string,
  rank: string,
  baseSalary: string,
  records: SalaryRecord[],
  nosAllTime: number,
  nocAllTime: number,
  createdBy: string,
  createdDate: string,
  lastModifiedBy: string,
  lastModifiedDate: string
}
