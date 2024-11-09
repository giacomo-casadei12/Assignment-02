package sap.ass02.rideservice.domain.ports.dataAccessPorts;

import sap.ass02.rideservice.domain.entities.Ride;

import java.util.List;

/**
 * The interface for accessing the database for rides.
 */
public interface RideDA {
    /**
     * Gets all the rides.
     *
     * @return a List of MutableRide containing all rides
     */
    List<Ride> getAllRides();

    /**
     * Gets all the ongoing rides.
     *
     * @return a List of MutableRide containing all ongoing rides
     */
    List<Ride> getAllOngoingRides();

    /**
     * Gets all rides done by a single user.
     *
     * @param userId the user id
     * @return a List of MutableRide containing all rides done by a single user
     */
    List<Ride> getAllRidesByUser(int userId);

    /**
     * Gets all rides that used the specified bike.
     *
     * @param eBikeId the bike id
     * @return a List of MutableRide containing all rides
     * that used the specified e bike
     */
    List<Ride> getAllRidesByEBike(int eBikeId);

    /**
     * Gets a single ride by its id.
     *
     * @param id the id of the ride
     * @return a MutableRide containing the ride requested
     */
    Ride getRideById(int id);

    /**
     * Gets the ongoing ride of the specified user.
     *
     * @param userId the user id
     * @return a MutableRide containing the ongoing ride of that user
     */
    Ride getOngoingRideByUserId(int userId);

    /**
     * Create a ride.
     *
     * @param userId  the user id of the user starting the ride
     * @param eBikeId the bike id of the bike used by the user
     * @return true if the ride was successfully created
     */
    boolean createRide(int userId, int eBikeId);

    /**
     * End a single ride.
     *
     * @param id the id of the ride to be ended
     * @return true if the ride was successfully ended
     */
    boolean endRide(int id);

    /**
     * Delete a single ride.
     *
     * @param id the id of the ride to be deleted
     * @return true if the ride was successfully deleted
     */
    boolean deleteRide(int id);
}
