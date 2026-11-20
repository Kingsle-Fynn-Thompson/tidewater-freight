package com.tidewater.freight;

import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.ShippingException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.Weight;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.HeavyItemSurcharge;
import com.tidewater.freight.repository.RateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ShippingCalculator {

    private static final int MONEY_SCALE = 2;

    private final RateRepository repository;
    private final HeavyItemSurcharge heavyItemSurcharge;

    public ShippingCalculator(RateRepository repository, HeavyItemSurcharge heavyItemSurcharge) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.heavyItemSurcharge = Objects.requireNonNull(heavyItemSurcharge, "heavyItemSurcharge");
    }

    public BigDecimal calculateCost(String zone, Weight weight, ServiceLevel serviceLevel) {
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(serviceLevel, "serviceLevel");

        ZoneRate rate = lookUpRate(zone);

        BigDecimal cost = rate.costFor(weight).add(heavyItemSurcharge.forWeight(weight));
        return serviceLevel.applyTo(cost).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private ZoneRate lookUpRate(String zone) {
        try {
            ZoneRate rate = repository.findRate(zone);
            if (rate == null) {
                throw new RateUnavailableException("Rate source returned no rate for zone: " + zone);
            }
            return rate;
        } catch (ShippingException e) {
            throw e; // already meaningful, let it through unchanged
        } catch (RuntimeException e) {
            throw new RateUnavailableException("Could not retrieve rates for zone: " + zone, e);
        }
    }
}