package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidWeightException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WeightTest {

    // ---------- valid weights ----------

    @Test
    void keepsTheValueItWasGiven() {
        Weight weight = Weight.ofKilograms(10);
        assertEquals(0, weight.kilograms().compareTo(new BigDecimal("10")));
    }

    @Test
    void acceptsAVerySmallPositiveWeight() {
        assertDoesNotThrow(() -> Weight.ofKilograms(0.001));
    }

    // ---------- invalid weights ----------

    @Test
    void rejectsZero() {
        assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(0));
    }

    @Test
    void rejectsANegativeWeight() {
        assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(-5));
    }

    @Test
    void rejectsNotANumber() {
        assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(Double.NaN));
    }

    @Test
    void rejectsPositiveInfinity() {
        assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(Double.POSITIVE_INFINITY));
    }

    @Test
    void rejectsNegativeInfinity() {
        assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(Double.NEGATIVE_INFINITY));
    }

    @Test
    void rejectsANullWeight() {
        assertThrows(InvalidWeightException.class, () -> new Weight(null));
    }

    @Test
    void errorMessageNamesTheRejectedValue() {
        InvalidWeightException thrown =
                assertThrows(InvalidWeightException.class, () -> Weight.ofKilograms(-1));
        assertTrue(thrown.getMessage().contains("-1"));
    }

    // ---------- comparison ----------

    @Test
    void isHeavierThanIsTrueForAGreaterWeight() {
        assertTrue(Weight.ofKilograms(30.1).isHeavierThan(Weight.ofKilograms(30)));
    }

    @Test
    void isHeavierThanIsFalseForAnEqualWeight() {
        assertFalse(Weight.ofKilograms(30).isHeavierThan(Weight.ofKilograms(30)));
    }

    @Test
    void isHeavierThanIsFalseForALighterWeight() {
        assertFalse(Weight.ofKilograms(29.9).isHeavierThan(Weight.ofKilograms(30)));
    }
}