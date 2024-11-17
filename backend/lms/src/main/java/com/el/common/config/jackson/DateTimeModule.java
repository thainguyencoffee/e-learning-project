package com.el.common.config.jackson;

import com.el.common.TimeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class DateTimeModule extends SimpleModule {

    public DateTimeModule() {
        addSerializer(LocalDateTime.class, new DateTimeSerializer());
    }

    static class DateTimeSerializer extends StdSerializer<LocalDateTime> {

        public DateTimeSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (instant != null) {
                String formattedInstant = TimeUtils.FORMATTER.format(instant);
                jsonGenerator.writeString(formattedInstant);
            } else {
                jsonGenerator.writeNull();
            }
        }
    }

}
