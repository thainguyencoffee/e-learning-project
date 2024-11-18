export interface Bonus {
  type: string,
  amount: string,
}

export interface SalaryRecord {
  id: number,
  bonus: Bonus,
  createdDate: string,
  paidDate: string,
  nocByMonth: number,
  nosByMonth: number,
  totalAmount: number,
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
