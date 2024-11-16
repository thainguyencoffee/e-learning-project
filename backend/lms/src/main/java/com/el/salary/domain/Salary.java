package com.el.salary.domain;

import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Table("salary")
@Getter
public class Salary {
    @Id
    private Long id;
    private String teacher;
    private Rank rank;
    private MonetaryAmount baseSalary;
    @MappedCollection(idColumn = "salary")
    private Set<SalaryRecord> records = new HashSet<>();
    private Integer nosAllTime;
    private Integer nocAllTime;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;

    public Salary(String teacher) {
        if (teacher == null || teacher.isBlank())
            throw new InputInvalidException("Teacher cannot be null");

        this.teacher = teacher;

        rank = Rank.NONE;
        baseSalary = Money.of(0, Currencies.USD);
        nosAllTime = 0;
        nocAllTime = 0;
    }

    public void addSalaryRecord(Integer nocByMonth, Integer nosByMonth) {
        if (nocByMonth == null || nocByMonth < 0)
            throw new InputInvalidException("Number of courses by month cannot be null or negative");
        if (nosByMonth == null || nosByMonth < 0)
            throw new InputInvalidException("Number of students by month cannot be null or negative");

        records.add(new SalaryRecord(Bonus.BonusType.ALL, nocByMonth, nosByMonth, baseSalary));
    }

    public void adjustNumberOfStudents() {
        nosAllTime++;
        adjustRank();
    }

    public void adjustNumberOfCourses() {
        nocAllTime++;
        adjustRank();
    }

    private void adjustRank() {
        if (nosAllTime >= 100 && nocAllTime >= 5) {
            rank = Rank.INSTRUCTOR_III;
        } else if (nosAllTime >= 50 && nocAllTime >= 3) {
            rank = Rank.INSTRUCTOR_II;
        } else if (nosAllTime >= 10 && nocAllTime >= 1) {
            rank = Rank.INSTRUCTOR_I;
        } else {
            rank = Rank.NONE;
        }

        calculateBaseSalary();
    }

    private void calculateBaseSalary() {
        if (rank == Rank.NONE) {
            baseSalary = Money.of(0, Currencies.USD);
            return;
        }
        switch (rank) {
            case INSTRUCTOR_I -> baseSalary = Money.of(300, Currencies.USD);
            case INSTRUCTOR_II -> baseSalary = Money.of(500, Currencies.USD);
            case INSTRUCTOR_III -> baseSalary = Money.of(600, Currencies.USD);
        }
    }

}
