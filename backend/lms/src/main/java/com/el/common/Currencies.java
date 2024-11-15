package com.el.common;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public interface Currencies {

	CurrencyUnit VND = Monetary.getCurrency("VND");

	CurrencyUnit USD = Monetary.getCurrency("USD");
}
