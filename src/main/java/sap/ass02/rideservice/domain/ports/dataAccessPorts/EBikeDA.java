package sap.ass02.rideservice.domain.ports.dataAccessPorts;

import sap.ass02.rideservice.domain.entities.EBike;

/**
 * The interface for accessing the database for electric bikes.
 */
public interface EBikeDA {

    /**
     * Gets a bike given its id.
     *
     * @param id the id of the bike
     * @return a MutableEBike containing the bike
     */
    EBike getEBikeById(int id);

    /**
     * Update all characteristics of the bike.
     *
     * @param bike the updated bike
     * @return true if the bike was successfully updated.
     */
    boolean updateEBike(EBike bike);

}
