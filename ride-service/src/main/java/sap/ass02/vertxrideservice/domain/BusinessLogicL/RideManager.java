package sap.ass02.vertxrideservice.domain.BusinessLogicL;


import sap.ass02.vertxrideservice.domain.entities.EBike;
import sap.ass02.vertxrideservice.domain.entities.User;
import sap.ass02.vertxrideservice.utils.Pair;

/**
 * The interface for the Ride manager that contains the logic for
 * handling an ongoing ride.
 */
public interface RideManager {


    /**
     * Start a ride.
     *
     * @param user the user that's doing the ride
     * @param bike the bike used in the ride
     * @return if the ride was successfully started
     */
    boolean startRide(User user, EBike bike);

    /**
     * Update a ride.
     *
     * @param user        the user that's doing the ride
     * @param bike        the bike used in the ride
     * @param x           the new x coordinate for the bike
     * @param y           the new y coordinate for the bike
     * @param timeElapsed the time elapsed since the last update
     * @return a Pair<Integer, Integer> containing the credit left for the
     * user and the battery left for the bike
     */
    Pair<Integer, Integer> updateRide(User user, EBike bike, int x, int y, long timeElapsed);

    /**
     * End a ride.
     *
     * @param user the user that's doing the ride
     * @param bike the bike used in the ride
     * @return true if the ride was successfully ended.
     */
    boolean endRide(User user, EBike bike);

    /**
     * Attach a Notification Service to notify the
     * changes to users, bikes and rides.
     *
     * @param notificationService the instance of a notificationService
     */
    void attachNotificationService(NotificationService notificationService);

    void updateConfigurations(double batteryConsumption, double creditConsumption);
}
