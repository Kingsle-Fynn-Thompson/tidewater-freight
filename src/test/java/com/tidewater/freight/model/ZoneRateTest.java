package com.tidewater.freight.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZoneRateTest {

    @Test
    void costIsRatePerKgTimesWeightPlusFee() {
        ZoneRate rate = ZoneRate.of("2.0", "3");
        BigDecimal cost = rate.costFor(Weight.ofKilograms(10));
        assertEquals(0, new BigDecimal("23").compareTo(cost)); // 2*10 + 3
    }

    @Test
    void aZeroFeeIsAllowed() {
        ZoneRate rate = ZoneRate.of("5.0", "0");
        assertEquals(0, new BigDecimal("5.0").compareTo(rate.ratePerKg()));
        assertEquals(0, BigDecimal.ZERO.compareTo(rate.flatFee()));
    }

    @Test
    void aZeroRateIsAllowed() {
        ZoneRate rate = ZoneRate.of("0", "10");
        assertEquals(0, BigDecimal.ZERO.compareTo(rate.ratePerKg()));
        assertEquals(0, new BigDecimal("10").compareTo(rate.flatFee()));
    }

    @Test
    void rejectsANegativeRate() {
        assertThrows(IllegalArgumentException.class, () -> ZoneRate.of("-1", "0"));
    }

    @Test
    void rejectsANegativeFee() {
        assertThrows(IllegalArgumentException.class, () -> ZoneRate.of("5", "-1"));
    }

    @Test
    void rejectsAMissingRate() {
        assertThrows(NullPointerException.class, () -> new ZoneRate(null, BigDecimal.ZERO));
    }

    @Test
    void rejectsAMissingFee() {
        assertThrows(NullPointerException.class, () -> new ZoneRate(BigDecimal.ONE, null));
    }

    @Test
    void ofParsesDecimalText() {
        ZoneRate rate = ZoneRate.of("8.5", "10");
        assertEquals(0, new BigDecimal("8.5").compareTo(rate.ratePerKg()));
        assertEquals(0, new BigDecimal("10").compareTo(rate.flatFee()));
    }
}