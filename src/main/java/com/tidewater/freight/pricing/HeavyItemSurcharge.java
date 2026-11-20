package com.tidewater.freight.pricing;

import com.tidewater.freight.model.Weight;
import java.math.BigDecimal;
import java.util.Objects;

public class HeavyItemSurcharge {

    private final Weight threshold;
    private final BigDecimal amount;

    public HeavyItemSurcharge(Weight threshold, BigDecimal amount) {
        this.threshold = Objects.requireNonNull(threshold, "threshold");
        this.amount = Objects.requireNonNull(amount, "amount");
    }

    public static HeavyItemSurcharge standard() {
        return new HeavyItemSurcharge(Weight.ofKilograms(30), new BigDecimal("20"));
    }

    /** Charged only when strictly heavier than the threshold, matching the original code. */
    public BigDecimal forWeight(Weight weight) {
        return weight.isHeavierThan(threshold) ? amount : BigDecimal.ZERO;
    }
}