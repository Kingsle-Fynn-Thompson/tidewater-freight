package com.tidewater.freight.model;

import com.tidewater.freight.exception.UnknownServiceLevelException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLevelTest {

    // ---------- parsing valid input ----------

    @Test
    void parsesStandard() {
        assertEquals(ServiceLevel.STANDARD, ServiceLevel.parse("STANDARD"));
    }

    @Test
    void parsesExpress() {
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.parse("EXPRESS"));
    }

    @Test
    void parsingIgnoresLowerCase() {
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.parse("express"));
    }

    @Test
    void parsingIgnoresMixedCase() {
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.parse("Express"));
    }

    @Test
    void parsingIgnoresSurroundingSpaces() {
        assertEquals(ServiceLevel.STANDARD, ServiceLevel.parse("  standard  "));
    }

    // ---------- parsing invalid input ----------

    @Test
    void rejectsNull() {
        assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse(null));
    }

    @Test
    void rejectsAnUnknownLevel() {
        assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse("OVERNIGHT"));
    }

    @Test
    void rejectsATypoInsteadOfQuietlyChargingStandard() {
        assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse("EXPRES"));
    }

    @Test
    void rejectsAnEmptyString() {
        assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse(""));
    }

    @Test
    void rejectsABlankString() {
        assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse("   "));
    }

    @Test
    void errorMessageNamesTheRejectedValue() {
        UnknownServiceLevelException thrown =
                assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse("OVERNIGHT"));
        assertTrue(thrown.getMessage().contains("OVERNIGHT"));
    }

    @Test
    void keepsTheOriginalCauseForAnUnknownLevel() {
        UnknownServiceLevelException thrown =
                assertThrows(UnknownServiceLevelException.class, () -> ServiceLevel.parse("OVERNIGHT"));
        assertNotNull(thrown.getCause());
    }

    // ---------- pricing ----------

    @Test
    void standardLeavesTheAmountUnchanged() {
        BigDecimal result = ServiceLevel.STANDARD.applyTo(new BigDecimal("100"));
        assertEquals(0, result.compareTo(new BigDecimal("100")));
    }

    @Test
    void expressMultipliesTheAmountByOnePointFive() {
        BigDecimal result = ServiceLevel.EXPRESS.applyTo(new BigDecimal("100"));
        assertEquals(0, result.compareTo(new BigDecimal("150")));
    }
}