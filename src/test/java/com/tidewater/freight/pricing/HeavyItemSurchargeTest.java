package com.tidewater.freight.pricing;

import com.tidewater.freight.model.Weight;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HeavyItemSurchargeTest {

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "expected " + expected + " but was " + actual);
    }

    // ---------- the standard Tidewater rule: above 30 kg, add 20 ----------

    @Test
    void standardRuleChargesNothingAtExactlyThirtyKilograms() {
        assertAmount("0", HeavyItemSurcharge.standard().forWeight(Weight.ofKilograms(30)));
    }

    @Test
    void standardRuleChargesTwentyJustAboveThirtyKilograms() {
        assertAmount("20", HeavyItemSurcharge.standard().forWeight(Weight.ofKilograms(30.01)));
    }

    @Test
    void standardRuleChargesNothingJustBelowThirtyKilograms() {
        assertAmount("0", HeavyItemSurcharge.standard().forWeight(Weight.ofKilograms(29.99)));
    }

    @Test
    void standardRuleChargesNothingForALightItem() {
        assertAmount("0", HeavyItemSurcharge.standard().forWeight(Weight.ofKilograms(1)));
    }

    @Test
    void surchargeIsAFlatAmountNotChargedPerKilogram() {
        HeavyItemSurcharge surcharge = HeavyItemSurcharge.standard();
        assertAmount("20", surcharge.forWeight(Weight.ofKilograms(31)));
        assertAmount("20", surcharge.forWeight(Weight.ofKilograms(100)));
    }

    // ---------- custom rules: proves the numbers come from the constructor ----------

    @Test
    void usesTheThresholdAndAmountItWasGiven() {
        HeavyItemSurcharge custom = new HeavyItemSurcharge(Weight.ofKilograms(5), new BigDecimal("7.50"));

        assertAmount("0", custom.forWeight(Weight.ofKilograms(5)));
        assertAmount("7.50", custom.forWeight(Weight.ofKilograms(5.1)));
    }

    @Test
    void aZeroAmountRuleNeverChargesAnything() {
        HeavyItemSurcharge free = new HeavyItemSurcharge(Weight.ofKilograms(5), BigDecimal.ZERO);

        assertAmount("0", free.forWeight(Weight.ofKilograms(500)));
    }

    // ---------- construction ----------

    @Test
    void rejectsAMissingThreshold() {
        assertThrows(NullPointerException.class,
                () -> new HeavyItemSurcharge(null, new BigDecimal("20")));
    }

    @Test
    void rejectsAMissingAmount() {
        assertThrows(NullPointerException.class,
                () -> new HeavyItemSurcharge(Weight.ofKilograms(30), null));
    }
}