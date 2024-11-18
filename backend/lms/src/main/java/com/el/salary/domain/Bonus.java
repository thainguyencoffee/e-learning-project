package com.el.salary.domain;

import com.el.common.Currencies;
import com.el.common.MoneyUtils;
import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;

public record Bonus(
        BonusType type,
        MonetaryAmount amount
) {

    public Bonus {
        if (type == null) {
            throw new InputInvalidException("Bonus type cannot be null");
        }
    }

    public Bonus(BonusType type, Integer numberOfStudents, Integer numberOfCourses) {
        this(type, calculateBonus(type, numberOfStudents, numberOfCourses));
    }

    static MonetaryAmount calculateBonus(BonusType type, Integer numberOfStudents, Integer numberOfCourses) {
        int rate = 2;
        int dollarPerUnitForStudent = 10;
        int dollarPerUnitForCourse = 20;

        return switch (type) {
            case NUMBER_OF_STUDENTS -> {
                int unit = numberOfStudents / rate;
                Money amount = Money.of(dollarPerUnitForStudent * unit, Currencies.USD);
                MoneyUtils.checkValidPrice(amount, Currencies.USD);
                yield amount;
            }
            case NUMBER_OF_COURSES -> {
                int unit2 = numberOfCourses / rate;
                Money amount = Money.of(dollarPerUnitForCourse * unit2, Currencies.USD);
                MoneyUtils.checkValidPrice(amount, Currencies.USD);
                yield amount;
            }
            case ALL -> {
                int unit = numberOfStudents / rate;
                int unit2 = numberOfCourses / rate;
                Money amount = Money.of(dollarPerUnitForStudent * unit + dollarPerUnitForCourse * unit2, Currencies.USD);
                MoneyUtils.checkValidPrice(amount, Currencies.USD);
                yield amount;
            }
        };
    }

    public enum BonusType {
        NUMBER_OF_STUDENTS,
        NUMBER_OF_COURSES,
        ALL
    }
}
