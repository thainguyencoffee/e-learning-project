package com.el.common;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.Set;

public interface Currencies {

	CurrencyUnit VND = Monetary.getCurrency("VND");

	CurrencyUnit USD = Monetary.getCurrency("USD");

	CurrencyUnit EUR = Monetary.getCurrency("EUR");

	Set<CurrencyUnit> SUPPORTED_CURRENCIES = Set.of(VND, USD);
}
