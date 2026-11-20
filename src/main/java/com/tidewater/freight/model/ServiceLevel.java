package com.tidewater.freight.model;

import com.tidewater.freight.exception.UnknownServiceLevelException;
import java.math.BigDecimal;
import java.util.Locale;

public enum ServiceLevel {
    STANDARD("1.0"),
    EXPRESS("1.5");

    private final BigDecimal multiplier;

    ServiceLevel(String multiplier) {
        this.multiplier = new BigDecimal(multiplier);
    }

    public BigDecimal applyTo(BigDecimal amount) {
        return amount.multiply(multiplier);
    }

    public static ServiceLevel parse(String value) {
        if (value == null) {
            throw new UnknownServiceLevelException("Service level must not be null");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UnknownServiceLevelException("Unknown service level: " + value, e);
        }
    }
}