package com.elearning.common.config;

import org.javamoney.moneta.Money;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class MonetaryAmountConverter extends AbstractJdbcConfiguration {

    @WritingConverter
    static class MonetaryAmountToString implements Converter<MonetaryAmount, String> {
        @Override
        public String convert(MonetaryAmount amount) {
            return String.format("%s %s", amount.getCurrency().toString(), amount.getNumber().toString());
        }
    }

    @ReadingConverter
    static class StringToMonetaryAmount implements Converter<String, MonetaryAmount> {
        // Define a custom format for parsing monetary amounts
        private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

        @Override
        public MonetaryAmount convert(@NotNull String source) {
            try {
                // Attempt to parse the string using the default Money parser
                return Money.parse(source);
            } catch (RuntimeException e) {
                try {
                    // If parsing fails, attempt to parse using the custom format
                    return Money.parse(source, FORMAT);
                } catch (RuntimeException inner) {
                    // Rethrow the original exception if parsing with the custom format also fails
                    throw e;
                }
            }
        }
    }

    @NotNull
    @Override
    protected List<?> userConverters() {
        return Arrays.asList(new MonetaryAmountToString(), new StringToMonetaryAmount());
    }
}
