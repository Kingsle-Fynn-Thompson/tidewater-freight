package com.tidewater.freight.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ZoneRate(BigDecimal ratePerKg, BigDecimal flatFee) {

    public ZoneRate {
        Objects.requireNonNull(ratePerKg, "ratePerKg");
        Objects.requireNonNull(flatFee, "flatFee");
        if (ratePerKg.signum() < 0 || flatFee.signum() < 0) {
            throw new IllegalArgumentException("Rates and fees must not be negative");
        }
    }

    public static ZoneRate of(String ratePerKg, String flatFee) {
        return new ZoneRate(new BigDecimal(ratePerKg), new BigDecimal(flatFee));
    }

    public BigDecimal costFor(Weight weight) {
        return ratePerKg.multiply(weight.kilograms()).add(flatFee);
    }
}