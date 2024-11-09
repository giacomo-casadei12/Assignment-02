package sap.ass02.bikeservice.domain.ports.dataAccessPorts;

import sap.ass02.bikeservice.domain.entities.EBike;

import java.util.List;

/**
 * The interface for accessing the database for electric bikes.
 */
public interface EBikeDA {
    /**
     * Gets all the bikes.
     *
     * @return a List of MutableEBike containing all bikes
     */
    List<EBike> getAllEBikes();

    /**
     * Gets all available bikes.
     *
     * @return a List of MutableEBike containing all available bikes
     */
    List<EBike> getAllAvailableEBikes();

    /**
     * Gets all the bikes nearby a specified position.
     *
     * @param positionX the x coordinate
     * @param positionY the y coordinate
     * @return a List of MutableEBike containing all the bikes nearby
     */
    List<EBike> getAllEBikesNearby(int positionX, int positionY);

    /**
     * Gets a bike given its id.
     *
     * @param id the id of the bike
     * @return a MutableEBike containing the bike
     */
    EBike getEBikeById(int id);

    /**
     * Create a new electric bike.
     *
     * @param positionX the initial x coordinate
     * @param positionY the initial y coordinate
     * @return true if the bike was successfully created.
     */
    boolean createEBike(int positionX, int positionY);

    /**
     * Update all characteristics of the bike.
     *
     * @param bike the updated bike
     * @return true if the bike was successfully updated.
     */
    boolean updateEBike(EBike bike);

    /**
     * Delete a bike given its id.
     *
     * @param id the id of the bike to be deleted.
     * @return true if the bike was successfully deleted.
     */
    boolean deleteEBike(int id);
}
