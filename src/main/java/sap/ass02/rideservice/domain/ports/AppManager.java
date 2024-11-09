package sap.ass02.rideservice.domain.ports;


import sap.ass02.rideservice.utils.Pair;

/**
 * The access point to all methods regarding the
 * persistence of Users, EBikes and Rides.
 */
public interface AppManager extends UserPersistence, EBikePersistence, RidePersistence {

    /**
     * Start a ride given a userId and a bikeID.
     *
     * @param userID  the user id
     * @param bikeID the e bike id
     * @return true if the ride was successfully created
     */
    boolean startRide(int userID, int bikeID);

    /**
     * Update the position of the bike in the ride.
     *
     * @param userID    the user id of the ride
     * @param bikeID   the e bike id of the bike used in the ride
     * @param x the actual x coordinate
     * @param y the actual Y coordinate
     * @return a Pair of Integers that contains the credit left for
     * the user and residual battery of the bike
     */
    Pair<Integer, Integer> updateRide(int userID, int bikeID, int x, int y);

    /**
     * End a ride given a userId and a bikeID.
     *
     * @param userID  the user id
     * @param bikeID the e bike id
     * @return true if the ride was successfully deleted
     */
    boolean endRide(int userID, int bikeID);

}
