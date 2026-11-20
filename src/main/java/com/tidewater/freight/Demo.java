package com.tidewater.freight;

import com.tidewater.freight.exception.ShippingException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.Weight;
import com.tidewater.freight.pricing.HeavyItemSurcharge;
import com.tidewater.freight.repository.InMemoryRateRepository;

import java.util.Locale;

public class Demo {

    public static void main(String[] args) {
        ShippingCalculator calculator =
                new ShippingCalculator(new InMemoryRateRepository(), HeavyItemSurcharge.standard());

        show(calculator, "DOMESTIC", 10, "STANDARD");
        show(calculator, "INTERNATIONAL", 40, "EXPRESS");
        show(calculator, "MOON", 10, "STANDARD");
    }

    private static void show(ShippingCalculator calculator, String zone, double kg, String level) {
        String request = String.format(Locale.ROOT,
                "Zone: %-13s Weight: %5.1f kg  Service: %-8s", zone, kg, level);
        try {
            var cost = calculator.calculateCost(zone, Weight.ofKilograms(kg), ServiceLevel.parse(level));
            System.out.println(request + " -> GHS " + cost);
        } catch (ShippingException e) {
            System.out.println(request + " -> REJECTED: " + e.getMessage());
        }
    }
}