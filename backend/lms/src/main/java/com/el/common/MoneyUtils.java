package com.el.common;

import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.util.Set;

public class MoneyUtils {

    public static void checkValidPrice(MonetaryAmount price) {
        var validCurrencies = Set.of(Currencies.VND, Currencies.USD);
        boolean isContainsCurrency = validCurrencies.contains(price.getCurrency());
        if (!isContainsCurrency) {
            throw new InputInvalidException("Currency is not supported. We support VND and USD only.");
        }
        if (price.getCurrency() == Currencies.VND) {
            checkValidVNDCurrency(price);
        } else {
            checkValidUSDCurrency(price);
        }
    }

    private static void checkValidUSDCurrency(MonetaryAmount price) {
        if (price.isGreaterThan(Money.of(10_000, Currencies.USD))) {
            throw new InputInvalidException("Price must not exceed 10,000 USD.");
        }
        if (price.isLessThan(Money.zero(price.getCurrency()))) {
            throw new InputInvalidException("Price cannot be negative.");
        }
    }

    private static void checkValidVNDCurrency(MonetaryAmount price) {
        if (price.getNumber().intValue() % 1000 != 0) {
            throw new InputInvalidException("Invalid Vietnamese Dong amount. Must be a multiple of 1000.");
        }
        if (price.isGreaterThan(Money.of(1_000_000_000, Currencies.VND))) {
            throw new InputInvalidException("Price must not exceed 1,000,000,000 VND.");
        }
        if (price.isLessThan(Money.zero(price.getCurrency()))) {
            throw new InputInvalidException("Price cannot be negative.");
        }
    }

    public static void checkValidPrice(MonetaryAmount price, CurrencyUnit currencyUnit) {
        var validCurrencies = Set.of(Currencies.VND, Currencies.USD);
        boolean isContainsCurrency = validCurrencies.contains(price.getCurrency()) && validCurrencies.contains(currencyUnit);
        if (!isContainsCurrency) {
            throw new InputInvalidException("Currency is not supported. We support VND and USD only.");
        }

        if (price.getCurrency() != currencyUnit) {
            throw new InputInvalidException("Currency is not supported. We support " + currencyUnit.getCurrencyCode() + " only.");
        } else {
            if (currencyUnit == Currencies.VND) {
                checkValidVNDCurrency(price);
            } else {
                checkValidUSDCurrency(price);
            }
        }
    }

}
