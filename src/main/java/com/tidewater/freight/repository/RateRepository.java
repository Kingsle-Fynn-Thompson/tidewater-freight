package com.tidewater.freight.repository;

import com.tidewater.freight.model.ZoneRate;

public interface RateRepository {
    /**
     * @throws com.tidewater.freight.exception.UnknownZoneException if the zone has no rates
     */
    ZoneRate findRate(String zone);
}