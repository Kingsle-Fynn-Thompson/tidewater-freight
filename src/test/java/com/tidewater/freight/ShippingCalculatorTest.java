package com.tidewater.freight;

import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.Weight;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.HeavyItemSurcharge;
import com.tidewater.freight.repository.RateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ShippingCalculatorTest {

    private RateRepository repository;
    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        repository = mock(RateRepository.class);
        calculator = new ShippingCalculator(repository, HeavyItemSurcharge.standard());
    }

    /** A made-up zone with rate 2.0 per kg and fee 3, so no number comes from the real rate table. */
    private void stubTestZone() {
        when(repository.findRate("TESTZONE")).thenReturn(ZoneRate.of("2.0", "3"));
    }

    private BigDecimal cost(double kg, ServiceLevel level) {
        return calculator.calculateCost("TESTZONE", Weight.ofKilograms(kg), level);
    }

    // ---------- happy path ----------

    @Test
    void standardCostIsRateTimesWeightPlusFee() {
        stubTestZone();
        assertEquals(new BigDecimal("23.00"), cost(10, ServiceLevel.STANDARD)); // 2*10 + 3
    }

    @Test
    void expressMultipliesTheCostByOnePointFive() {
        stubTestZone();
        assertEquals(new BigDecimal("34.50"), cost(10, ServiceLevel.EXPRESS)); // 23 * 1.5
    }

    @Test
    void heavyItemSurchargeIsAddedAboveThirtyKilograms() {
        stubTestZone();
        assertEquals(new BigDecimal("85.00"), cost(31, ServiceLevel.STANDARD)); // 62 + 3 + 20
    }

    @Test
    void expressAlsoMultipliesTheHeavyItemSurcharge() {
        stubTestZone();
        assertEquals(new BigDecimal("127.50"), cost(31, ServiceLevel.EXPRESS)); // (62 + 3 + 20) * 1.5
    }

    // ---------- edge cases ----------

    @Test
    void exactlyThirtyKilogramsIsNotHeavy() {
        stubTestZone();
        assertEquals(new BigDecimal("63.00"), cost(30, ServiceLevel.STANDARD)); // 60 + 3, no surcharge
    }

    @Test
    void costIsRoundedHalfUpToTwoDecimalPlaces() {
        when(repository.findRate("TESTZONE")).thenReturn(ZoneRate.of("0.345", "0"));
        assertEquals(new BigDecimal("0.35"), cost(1, ServiceLevel.STANDARD)); // 0.345 rounds up
    }

    @Test
    void usesTheInjectedSurchargeRatherThanAHardCodedOne() {
        stubTestZone();
        HeavyItemSurcharge custom = new HeavyItemSurcharge(Weight.ofKilograms(5), new BigDecimal("1"));
        ShippingCalculator customCalculator = new ShippingCalculator(repository, custom);

        BigDecimal result = customCalculator.calculateCost(
                "TESTZONE", Weight.ofKilograms(5.5), ServiceLevel.STANDARD);

        assertEquals(new BigDecimal("15.00"), result); // 11 + 3 + 1
    }

    // ---------- failure cases ----------

    @Test
    void unknownZoneIsRejectedAndNeverPricedAsZero() {
        UnknownZoneException original = new UnknownZoneException("MOON");
        when(repository.findRate("MOON")).thenThrow(original);

        UnknownZoneException thrown = assertThrows(UnknownZoneException.class,
                () -> calculator.calculateCost("MOON", Weight.ofKilograms(10), ServiceLevel.STANDARD));

        assertSame(original, thrown);
        assertEquals("MOON", thrown.getZone());
    }

    @Test
    void unexpectedRepositoryFailureIsWrappedWithItsCause() {
        IllegalStateException dbDown = new IllegalStateException("database down");
        when(repository.findRate("TESTZONE")).thenThrow(dbDown);

        RateUnavailableException thrown = assertThrows(RateUnavailableException.class,
                () -> cost(10, ServiceLevel.STANDARD));

        assertSame(dbDown, thrown.getCause());
        assertTrue(thrown.getMessage().contains("TESTZONE"));
    }

    @Test
    void repositoryReturningNoRateIsReportedAsUnavailable() {
        when(repository.findRate("TESTZONE")).thenReturn(null);

        assertThrows(RateUnavailableException.class, () -> cost(10, ServiceLevel.STANDARD));
    }

    @Test
    void nullWeightIsRejectedBeforeAnyLookup() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculateCost("TESTZONE", null, ServiceLevel.STANDARD));
        verifyNoInteractions(repository);
    }

    @Test
    void nullServiceLevelIsRejectedBeforeAnyLookup() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculateCost("TESTZONE", Weight.ofKilograms(10), null));
        verifyNoInteractions(repository);
    }

    @Test
    void constructorRejectsMissingDependencies() {
        assertThrows(NullPointerException.class,
                () -> new ShippingCalculator(null, HeavyItemSurcharge.standard()));
        assertThrows(NullPointerException.class,
                () -> new ShippingCalculator(repository, null));
    }
}