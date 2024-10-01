package com.elearning.common;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public interface Currencies {

	static final CurrencyUnit VND = Monetary.getCurrency("VND");
}
