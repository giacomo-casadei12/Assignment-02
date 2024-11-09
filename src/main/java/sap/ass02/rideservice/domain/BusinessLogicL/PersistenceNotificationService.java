package sap.ass02.rideservice.domain.BusinessLogicL;

import sap.ass02.rideservice.domain.entities.EBike;
import sap.ass02.rideservice.domain.entities.User;

/**
 * The interface that will detect any changes done to entities by a RideManager.
 */
public interface PersistenceNotificationService {

    /**
     * Notify the update of a user.
     *
     * @param user the updated user
     */
    void notifyUpdateUser(User user);

    /**
     * Notify the update of a bike.
     *
     * @param bike the updated bike
     */
    void notifyUpdateEBike(EBike bike);

    /**
     * Notify the end of a ride.
     *
     * @param user the user that did the ride
     * @param bike the bike used in the ride
     */
    void notifyEndRide(User user, EBike bike);

}
