package com.tidewater.freight.repository;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateRepositoryTest {

    private static void assertRate(String expectedRatePerKg, String expectedFlatFee, ZoneRate actual) {
        assertEquals(0, new BigDecimal(expectedRatePerKg).compareTo(actual.ratePerKg()),
                "rate per kg was " + actual.ratePerKg());
        assertEquals(0, new BigDecimal(expectedFlatFee).compareTo(actual.flatFee()),
                "flat fee was " + actual.flatFee());
    }

    // ---------- the real Tidewater zones ----------

    @Test
    void domesticHasRateFiveAndNoFee() {
        assertRate("5", "0", new InMemoryRateRepository().findRate("DOMESTIC"));
    }

    @Test
    void regionalHasRateEightAndHandlingFeeTen() {
        assertRate("8", "10", new InMemoryRateRepository().findRate("REGIONAL"));
    }

    @Test
    void internationalHasRateFifteenAndCustomsFeeTwentyFive() {
        assertRate("15", "25", new InMemoryRateRepository().findRate("INTERNATIONAL"));
    }

    // ---------- unknown zones are rejected, never priced ----------

    @Test
    void unknownZoneThrowsInsteadOfReturningARate() {
        assertThrows(UnknownZoneException.class, () -> new InMemoryRateRepository().findRate("MOON"));
    }

    @Test
    void unknownZoneErrorNamesTheZone() {
        UnknownZoneException thrown = assertThrows(UnknownZoneException.class,
                () -> new InMemoryRateRepository().findRate("MOON"));

        assertEquals("MOON", thrown.getZone());
        assertTrue(thrown.getMessage().contains("MOON"));
    }

    @Test
    void nullZoneIsRejectedAsUnknown() {
        UnknownZoneException thrown = assertThrows(UnknownZoneException.class,
                () -> new InMemoryRateRepository().findRate(null));

        assertNull(thrown.getZone());
    }

    @Test
    void emptyZoneIsRejectedAsUnknown() {
        assertThrows(UnknownZoneException.class, () -> new InMemoryRateRepository().findRate(""));
    }

    @Test
    void zoneNamesAreCaseSensitive() {
        // Matches the original behaviour. A lower-case name is a different, unknown zone.
        assertThrows(UnknownZoneException.class, () -> new InMemoryRateRepository().findRate("domestic"));
    }

    // ---------- adding a zone is data, not code ----------

    @Test
    void aNewZoneCanBeAddedWithoutChangingAnyLogic() {
        Map<String, ZoneRate> rates = new HashMap<>();
        rates.put("NEWZONE", ZoneRate.of("20.0", "40"));

        InMemoryRateRepository repository = new InMemoryRateRepository(rates);

        assertRate("20", "40", repository.findRate("NEWZONE"));
    }

    @Test
    void aCustomRepositoryDoesNotInheritTheDefaultZones() {
        Map<String, ZoneRate> rates = Map.of("NEWZONE", ZoneRate.of("20.0", "40"));

        InMemoryRateRepository repository = new InMemoryRateRepository(rates);

        assertThrows(UnknownZoneException.class, () -> repository.findRate("DOMESTIC"));
    }

    // ---------- protection against outside changes ----------

    @Test
    void changingTheOriginalMapAfterwardsDoesNotChangeTheRepository() {
        Map<String, ZoneRate> rates = new HashMap<>();
        rates.put("ZONE_A", ZoneRate.of("1.0", "0"));
        InMemoryRateRepository repository = new InMemoryRateRepository(rates);

        rates.put("ZONE_B", ZoneRate.of("2.0", "0"));
        rates.remove("ZONE_A");

        assertRate("1", "0", repository.findRate("ZONE_A"));
        assertThrows(UnknownZoneException.class, () -> repository.findRate("ZONE_B"));
    }

    @Test
    void rejectsAMissingRateMap() {
        assertThrows(NullPointerException.class, () -> new InMemoryRateRepository(null));
    }
}