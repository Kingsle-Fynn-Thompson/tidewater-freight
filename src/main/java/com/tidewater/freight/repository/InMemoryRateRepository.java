package com.tidewater.freight.repository;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;
import java.util.Map;

public class InMemoryRateRepository implements RateRepository {

    private final Map<String, ZoneRate> rates;

    public InMemoryRateRepository() {
        this(defaultRates());
    }

    public InMemoryRateRepository(Map<String, ZoneRate> rates) {
        this.rates = Map.copyOf(rates);
    }

    private static Map<String, ZoneRate> defaultRates() {
        return Map.of(
                "DOMESTIC", ZoneRate.of("5.0", "0"),
                "REGIONAL", ZoneRate.of("8.0", "10"),
                "INTERNATIONAL", ZoneRate.of("15.0", "25"));
    }

    @Override
    public ZoneRate findRate(String zone) {
        ZoneRate rate = (zone == null) ? null : rates.get(zone);
        if (rate == null) {
            throw new UnknownZoneException(zone);
        }
        return rate;
    }
}