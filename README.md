# Tidewater Freight: Code Quality and Testing Lab

A small shipping-cost service for Tidewater Freight, refactored from AI-generated starter code to Clean Code and SOLID standards, with a test suite that proves real behaviour.

The full written analysis, results and rationale are in **[ANALYSIS.md](ANALYSIS.md)**.

## What the service does

It works out the cost of a shipment from three inputs: the destination **zone**, the **weight**, and the **service level**.

```
cost = (rate per kg x weight + zone fee + heavy-item surcharge) x service-level multiplier
```

| Rule | Value |
|---|---|
| DOMESTIC | 5.0 per kg, no fee |
| REGIONAL | 8.0 per kg, fee 10 |
| INTERNATIONAL | 15.0 per kg, fee 25 |
| Heavy-item surcharge | 20, only when weight is above 30 kg |
| STANDARD | x 1.0 |
| EXPRESS | x 1.5 (also applies to the surcharge) |

The result is a `BigDecimal` in GHS, rounded to 2 decimal places (HALF_UP).

## Results at a glance

| Measure | Inherited code | Refactored |
|---|---|---|
| Tests | 3 | 75 |
| Mutants killed (PIT) | 3 of 18 (17%) | 22 of 22 (100%) |
| Test strength | 27% | 100% |
| Mutants with no coverage | 7 | 0 |
| Line coverage (mutated classes) | 61% | 100% |
| Unknown zone `MOON` | silently returned `0.0` | rejected with `UnknownZoneException` |

The two sets of mutation numbers are not strictly like-for-like, because the code was restructured and `Demo` is now excluded from mutation scope. See ANALYSIS.md section 8 for the honest reading.

## Running it

You need **JDK 21** and **Maven 3.9** .

Run the demo:

```
mvn compile
mvn exec:java "-Dexec.mainClass=com.tidewater.freight.Demo"
```

Expected output:

```
DOMESTIC 10.0kg STANDARD -> GHS 50.00
INTERNATIONAL 40.0kg EXPRESS -> GHS 967.50
MOON 10.0kg STANDARD -> REJECTED: Unknown zone: MOON
```

Run the tests:

```
mvn test
```

Run everything, including coverage and the mutation check:

```
mvn clean verify
```

- Coverage report: `target/site/jacoco/index.html`
- Mutation report: `target/pit-reports/index.html`

`mvn verify` fails if the mutation score drops below 90% (see "Improvement beyond the brief").

Run mutation testing on its own:

```
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

## Project structure

```
src/main/java/com/tidewater/freight
├── Demo.java                          manual demo entry point
├── ShippingCalculator.java            orchestrates the calculation only
├── exception/
│   ├── ShippingException.java         unchecked base class
│   ├── InvalidWeightException.java
│   ├── UnknownZoneException.java
│   ├── UnknownServiceLevelException.java
│   └── RateUnavailableException.java  wraps an unexpected failure, keeps the cause
├── model/
│   ├── Weight.java                    validated on construction
│   ├── ServiceLevel.java              enum with its multiplier
│   └── ZoneRate.java                  rate per kg and flat fee, together
├── pricing/
│   └── HeavyItemSurcharge.java        threshold and amount, injectable
└── repository/
    ├── RateRepository.java            interface: the injected seam
    └── InMemoryRateRepository.java    default zone data

src/test/java/com/tidewater/freight
├── CharacterizationTest.java          pins the original prices
├── ShippingCalculatorTest.java        Mockito: happy, edge and failure paths
├── model/       WeightTest, ServiceLevelTest, ZoneRateTest
├── pricing/     HeavyItemSurchargeTest
└── repository/  InMemoryRateRepositoryTest
```

## Design summary

- **One job per class.** The calculator only orchestrates. Validation, rate data and the heavy-item rule each live in their own class.
- **Dependencies are injected.** `ShippingCalculator` receives its `RateRepository` and `HeavyItemSurcharge` through the constructor, so tests can replace them.
- **Invalid input cannot exist.** `Weight` rejects null, zero, negative, `NaN` and infinity. `ServiceLevel` is an enum, so a typo is rejected instead of being charged as standard.
- **Open for the new zone.** Rate and fee live together in `ZoneRate`. Adding a zone is one line in `InMemoryRateRepository.defaultRates()`:

  ```java
  "NEWZONE", ZoneRate.of("20.0", "40")
  ```

  The calculator and the existing rules are untouched. The test `aNewZoneCanBeAddedWithoutChangingAnyLogic` demonstrates this.

## Exception handling

| Situation | Result |
|---|---|
| Unknown zone | `UnknownZoneException` (names the zone) |
| Invalid weight | `InvalidWeightException` |
| Unknown service level | `UnknownServiceLevelException` |
| Unexpected rate-source failure | `RateUnavailableException`, with the original cause attached |

The only `try/catch` is around the repository lookup. Our own exceptions pass through unchanged and anything unexpected is wrapped, so no failure is swallowed or hidden. The original code's `catch (Exception e) { return 0; }` is gone.

## Testing approach

- **Characterization tests** pin the original prices (including `967.50`), so the refactor cannot silently change a price.
- **`ShippingCalculatorTest`** mocks only the repository, never the class under test. It uses a made-up zone with its own numbers, so a calculator that ignored the repository would fail. Every test asserts a specific price or exception type.
- **Boundary tests** cover 29.99, 30 and 30.01 kg, and every invalid weight value.
- **The three inherited tests were deleted**, not kept. None of them could fail when the code was wrong. See ANALYSIS.md section 3.

## Improvement beyond the brief

PIT mutation testing runs automatically inside `mvn verify` and fails the build below a 90% score. The inherited suite was green while catching only 17% of bugs, and neither a passing run nor line coverage can detect that. The guard makes the question "do these tests really catch bugs?" part of every build. `Demo` is excluded because it holds no logic. Details and limits are in ANALYSIS.md section 9.6.

## Assumptions awaiting reviewer confirmation

These keep the original behaviour and are pinned by tests, so they are cheap to change:

1. Exactly 30 kg is not heavy (surcharge only above 30 kg).
2. The express multiplier also applies to the heavy-item surcharge.
3. A weight of zero is invalid.
4. Zone names are case-sensitive.
5. Money is rounded to 2 decimal places, HALF_UP.

## Known limits

- `Map.of` holds at most 10 zones. A larger table needs a different map construction.
- If a future zone needs different pricing logic (for example tiers by weight band), data alone will not be enough and a rate-rule interface should be introduced then.