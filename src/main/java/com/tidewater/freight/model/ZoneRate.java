package com.tidewater.freight.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The pricing for one shipping zone: a price per kilogram plus a flat fee
 * charged once per shipment.
 *
 * @param ratePerKg price per kilogram, never negative
 * @param flatFee   fixed fee added once per shipment, never negative
 */
public record ZoneRate(BigDecimal ratePerKg, BigDecimal flatFee) {

    public ZoneRate {
        Objects.requireNonNull(ratePerKg, "ratePerKg");
        Objects.requireNonNull(flatFee, "flatFee");
        if (ratePerKg.signum() < 0 || flatFee.signum() < 0) {
            throw new IllegalArgumentException("Rates and fees must not be negative");
        }
    }

    /**
     * Creates a zone rate from decimal text.
     *
     * <p>Argument order matters: the <b>rate per kilogram comes first</b> and the
     * flat fee second. For example, {@code ZoneRate.of("8.0", "10")} means 8.0 per kg
     * plus a flat fee of 10 per shipment.
     *
     * @param ratePerKg price per kilogram, for example {@code "8.0"}
     * @param flatFee   fixed fee added once per shipment, for example {@code "10"}
     */
    public static ZoneRate of(String ratePerKg, String flatFee) {
        return new ZoneRate(new BigDecimal(ratePerKg), new BigDecimal(flatFee));
    }

    /** Returns the zone cost for a weight: rate per kg times weight, plus the flat fee. */
    public BigDecimal costFor(Weight weight) {
        return ratePerKg.multiply(weight.kilograms()).add(flatFee);
    }
}