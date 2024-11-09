package sap.ass02.bikeservice.domain.ports;

import sap.ass02.bikeservice.domain.entities.EBike;
import sap.ass02.bikeservice.utils.EBikeState;

import java.util.List;

/**
 * The interface exposing method for retrieve
 * info from the Data Access Layer regarding the bikes.
 */
public interface EBikePersistence {

    /**
     * Gets all the bikes.
     *
     * @param positionX if greater than 0, the bikes returned
     *                  will be near this coordinate
     * @param positionY if greater than 0, the bikes returned
     *                  will be near this coordinate
     * @param available if true, return all the available bikes
     * @return a List of EBike containing bikes matching the criteria specified
     */
    List<EBike> getAllEBikes(int positionX, int positionY, boolean available);

    /**
     * Gets a bike given its id.
     *
     * @param id the id of the bike
     * @return an EBike containing the requested bike
     */
    EBike getEBike(int id);

    /**
     * Create a new bike.
     *
     * @param positionX the starting x coordinate
     * @param positionY the starting y coordinate
     * @return true if the bike was successfully created
     */
    boolean createEBike(int positionX, int positionY);

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

    /**
     * Update the actual position of the specified bike.
     *
     * @param id        the id of the bike to be modified
     * @param positionX the position x coordinate
     * @param positionY the position y coordinate
     * @return true if the bike was successfully updated
     */
    boolean updateEbikePosition(int id, int positionX, int positionY);

    /**
     * Delete the specified bike.
     *
     * @param id the id of the bike
     * @return true if the bike was successfully deleted
     */
    boolean deleteEBike(int id);

}
