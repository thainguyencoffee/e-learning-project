package com.el.salary.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;

@Table("salary_payment")
@Getter
public class SalaryRecord {
    @Id
    private Long id;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private Bonus bonus;
    private Instant createdDate;
    private Instant paidDate;
    private Integer nocByMonth;
    private Integer nosByMonth;
    private Integer totalAmount;
    private SalaryRecordStatus status;
    private String failureReason;

    public SalaryRecord(Bonus.BonusType bonusType, Integer nocByMonth, Integer nosByMonth, MonetaryAmount baseSalary) {
        if (baseSalary == null)
            throw new InputInvalidException("Base salary cannot be null");

        this.nocByMonth = nocByMonth;
        this.nosByMonth = nosByMonth;

        this.bonus = new Bonus(bonusType, nocByMonth, nosByMonth);
        this.createdDate = Instant.now();
        this.totalAmount = baseSalary.add(bonus.amount()).getNumber().intValue();
        this.status = SalaryRecordStatus.PENDING;
    }

    public void markAsPaid() {
        if (this.status == SalaryRecordStatus.PAID)
            throw new InputInvalidException("Salary record already paid");
        this.status = SalaryRecordStatus.PAID;
        this.paidDate = Instant.now();
    }

    public void markAsFailed(String reason) {
        if (this.status == SalaryRecordStatus.PAID)
            throw new InputInvalidException("Salary record already paid");
        this.status = SalaryRecordStatus.FAILED;
        this.failureReason = reason;
    }


}
