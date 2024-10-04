package com.elearning.common.config.jackson;

import com.elearning.common.TimeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

public class DateTimeModule extends SimpleModule {

    public DateTimeModule() {
        addSerializer(Instant.class, new DateTimeSerializer());
    }

    static class DateTimeSerializer extends StdSerializer<Instant> {

        public DateTimeSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (instant != null) {
                String formattedInstant = TimeUtils.FORMATTER.format(instant);
                jsonGenerator.writeString(formattedInstant);
            } else {
                jsonGenerator.writeNull();
            }
        }
    }

}
