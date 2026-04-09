package com.consignment.service.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;

public class EmptyStringToNullLocalDateDeserializer extends StdDeserializer<LocalDate> {

    public EmptyStringToNullLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
