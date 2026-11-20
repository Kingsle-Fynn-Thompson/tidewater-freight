package com.tidewater.freight;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.Weight;
import com.tidewater.freight.pricing.HeavyItemSurcharge;
import com.tidewater.freight.repository.InMemoryRateRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterizationTest {

    private final ShippingCalculator calculator =
            new ShippingCalculator(new InMemoryRateRepository(), HeavyItemSurcharge.standard());

    private BigDecimal cost(String zone, double kg, ServiceLevel level) {
        return calculator.calculateCost(zone, Weight.ofKilograms(kg), level);
    }

    @Test void domestic10kgStandard()      { assertEquals(new BigDecimal("50.00"),  cost("DOMESTIC", 10, ServiceLevel.STANDARD)); }
    @Test void regional5kgStandard()       { assertEquals(new BigDecimal("50.00"),  cost("REGIONAL", 5, ServiceLevel.STANDARD)); }
    @Test void international40kgExpress()  { assertEquals(new BigDecimal("967.50"), cost("INTERNATIONAL", 40, ServiceLevel.EXPRESS)); }
    @Test void domestic30kgNotHeavy()      { assertEquals(new BigDecimal("150.00"), cost("DOMESTIC", 30, ServiceLevel.STANDARD)); }
    @Test void domestic31kgIsHeavy()       { assertEquals(new BigDecimal("175.00"), cost("DOMESTIC", 31, ServiceLevel.STANDARD)); }
    @Test void domestic10kgExpress()       { assertEquals(new BigDecimal("75.00"),  cost("DOMESTIC", 10, ServiceLevel.EXPRESS)); }
    @Test void international10kgStandard() { assertEquals(new BigDecimal("175.00"), cost("INTERNATIONAL", 10, ServiceLevel.STANDARD)); }
}