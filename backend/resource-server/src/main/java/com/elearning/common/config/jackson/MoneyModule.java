package com.elearning.common.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.javamoney.moneta.Money;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryFormats;
import java.io.IOException;

public class MoneyModule extends SimpleModule {

    public MoneyModule() {
        addSerializer(MonetaryAmount.class, new MoneyModule.MonetaryAmountSerializer());
        addValueInstantiator(MonetaryAmount.class, new MoneyModule.MoneyInstantiator());
    }

    static class MonetaryAmountSerializer extends StdSerializer<MonetaryAmount> {

        public MonetaryAmountSerializer() {
            super(MonetaryAmount.class);
        }

        @Override
        public void serialize(MonetaryAmount value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            if (value != null) {
                gen.writeString(MonetaryFormats.getAmountFormat(LocaleContextHolder.getLocale()).format(value));
            } else {
                gen.writeNull();
            }
        }

    }

    static class MoneyInstantiator extends ValueInstantiator {

        @Override
        public String getValueTypeDesc() {
            return MonetaryAmount.class.toString();
        }

        @Override
        public boolean canCreateFromString() {
            return true;
        }

        @Override
        public Object createFromString(DeserializationContext context, String value) throws IOException {
            return Money.parse(value, MonetaryFormats.getAmountFormat(LocaleContextHolder.getLocale()));
        }
    }

}