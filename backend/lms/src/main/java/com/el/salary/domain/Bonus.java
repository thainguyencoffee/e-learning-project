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
        return switch (type) {
            case NUMBER_OF_STUDENTS -> {
                int unit = numberOfStudents / 10;
                Money amount = Money.of(10 * unit, Currencies.USD);
                MoneyUtils.checkValidPrice(amount, Currencies.USD);
                yield amount;
            }
            case NUMBER_OF_COURSES -> {
                int unit2 = numberOfCourses / 10;
                Money amount = Money.of(20 * unit2, Currencies.USD);
                MoneyUtils.checkValidPrice(amount, Currencies.USD);
                yield amount;
            }
            case ALL -> {
                int unit = numberOfStudents / 10;
                int unit2 = numberOfCourses / 10;
                Money amount = Money.of(10 * unit + 20 * unit2, Currencies.USD);
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
