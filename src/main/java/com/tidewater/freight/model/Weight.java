package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidWeightException;
import java.math.BigDecimal;

public record Weight(BigDecimal kilograms) {

    public Weight {
        if (kilograms == null) {
            throw new InvalidWeightException("Weight must not be null");
        }
        if (kilograms.signum() <= 0) {
            throw new InvalidWeightException("Weight must be greater than zero but was " + kilograms);
        }
    }

    public static Weight ofKilograms(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new InvalidWeightException("Weight must be a finite number but was " + value);
        }
        return new Weight(BigDecimal.valueOf(value));
    }

    public boolean isHeavierThan(Weight other) {
        return kilograms.compareTo(other.kilograms) > 0;
    }
}