package com.el.salary.domain;

import javax.money.MonetaryAmount;

public interface SalaryDTO {

    Long getId();

    String getTeacher();

    Rank getRank();

    MonetaryAmount getBaseSalary();

    Integer getNosAllTime();

    Integer getNocAllTime();

}
