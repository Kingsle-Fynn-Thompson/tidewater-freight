package com.tidewater.freight;

import com.tidewater.freight.exception.ShippingException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.Weight;
import com.tidewater.freight.pricing.HeavyItemSurcharge;
import com.tidewater.freight.repository.InMemoryRateRepository;

public class Demo {

    public static void main(String[] args) {
        ShippingCalculator calculator =
                new ShippingCalculator(new InMemoryRateRepository(), HeavyItemSurcharge.standard());

        show(calculator, "DOMESTIC", 10, "STANDARD");
        show(calculator, "INTERNATIONAL", 40, "EXPRESS");
        show(calculator, "MOON", 10, "STANDARD");
    }

    private static void show(ShippingCalculator calculator, String zone, double kg, String level) {
        try {
            var cost = calculator.calculateCost(zone, Weight.ofKilograms(kg), ServiceLevel.parse(level));
            System.out.println(zone + " " + kg + "kg " + level + " -> GHS " + cost);
        } catch (ShippingException e) {
            System.out.println(zone + " " + kg + "kg " + level + " -> REJECTED: " + e.getMessage());
        }
    }
}