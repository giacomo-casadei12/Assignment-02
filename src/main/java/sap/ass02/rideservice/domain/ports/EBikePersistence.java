package sap.ass02.rideservice.domain.ports;

import sap.ass02.rideservice.domain.entities.EBike;
import sap.ass02.rideservice.utils.EBikeState;

/**
 * The interface exposing method for retrieve
 * info from the Data Access Layer regarding the bikes.
 */
public interface EBikePersistence {

    /**
     * Gets a bike given its id.
     *
     * @param id the id of the bike
     * @return an EBike containing the requested bike
     */
    EBike getEBike(int id);

    /**
     * Update all characteristics of a bike.
     *
     * @param id        the id of the bike to be modified
     * @param battery   the new value for the battery level
     * @param state     an EBikeState containing new state of the bike
     * @param positionX the x coordinate
     * @param positionY the y coordinate
     * @return true if the bike was successfully updated
     */
    boolean updateEBike(int id, int battery, EBikeState state, int positionX, int positionY);


}
