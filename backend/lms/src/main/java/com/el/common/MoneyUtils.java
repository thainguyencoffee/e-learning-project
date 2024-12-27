package com.el.common;

import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

public class MoneyUtils {

    public static void checkValidPrice(MonetaryAmount price) {
        boolean isContainsCurrency = Currencies.SUPPORTED_CURRENCIES.contains(price.getCurrency());
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
            throw new InputInvalidException("Invalid Vietnamese Dong price. Must be a multiple of 1000.");
        }
        if (price.isGreaterThan(Money.of(1_000_000_000, Currencies.VND))) {
            throw new InputInvalidException("Price must not exceed 1,000,000,000 VND.");
        }
        if (price.isLessThan(Money.zero(price.getCurrency()))) {
            throw new InputInvalidException("Price cannot be negative.");
        }
    }

    public static void checkValidPrice(MonetaryAmount price, CurrencyUnit currencyUnit) {
        boolean isContainsCurrency = Currencies.SUPPORTED_CURRENCIES.contains(price.getCurrency()) && Currencies.SUPPORTED_CURRENCIES.contains(currencyUnit);
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

//    public static MonetaryAmount convertTo(MonetaryAmount price, CurrencyUnit currencyUnit) {
//        boolean isContainsCurrency = Currencies.SUPPORTED_CURRENCIES.contains(price.getCurrency()) && Currencies.SUPPORTED_CURRENCIES.contains(currencyUnit);
//        if (!isContainsCurrency) {
//            throw new InputInvalidException("Currency is not supported. We support VND and USD only.");
//        }
//
//        ExchangeRateProvider exchangeRateProvider = MonetaryConversions.getExchangeRateProvider();
//        CurrencyConversion currencyConversion = exchangeRateProvider.getCurrencyConversion(currencyUnit);
//        return price.with(currencyConversion);
//    }

}
