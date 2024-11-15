package sap.ass02.rideservice.domain.BusinessLogicL;

import sap.ass02.rideservice.domain.entities.EBike;
import sap.ass02.rideservice.domain.entities.User;
import sap.ass02.rideservice.utils.EBikeState;
import sap.ass02.rideservice.utils.Pair;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Implementation of RideManager interface.
 */
public class RideManagerImpl implements RideManager {

    final private static double BATTERY_CONSUMPTION_PER_METER = 0.5;
    final private static double CREDIT_CONSUMPTION_PER_SECOND = 1;
    private NotificationService notificationService;

    @Override
    public boolean startRide(User user, EBike bike) {
        boolean success = false;

        if (user.credit() > 0 && Objects.equals(bike.state(), EBikeState.AVAILABLE.toString())) {
            success = true;
            bike.setState(EBikeState.IN_USE.toString());
            if (this.notificationService != null) {
                this.notificationService.notifyUpdateEBike(bike);
            }
        }

        return success;
    }

    @Override
    public Pair<Integer, Integer> updateRide(User user, EBike bike, int x, int y, long timeElapsed) {

        boolean stopRide;

        updatedCredit(user, timeElapsed);

        stopRide = user.credit() <= 0;

        updatedBattery(bike, x, y);

        if (!stopRide) {
            stopRide = bike.battery() <= 0;
        }

        if (stopRide) {
            if (bike.battery() > 0) {
                bike.setState(EBikeState.AVAILABLE.toString());
            }
        }

        if (this.notificationService != null) {
            this.notificationService.notifyUpdateEBike(bike);
            this.notificationService.notifyUpdateUser(user);
            if (stopRide) this.notificationService.notifyEndRide(user, bike);
        }

        return new Pair<>(user.credit(),bike.battery());
    }

    @Override
    public boolean endRide(User user, EBike bike) {

        if (bike.battery() > 0) {
            bike.setState(EBikeState.AVAILABLE.toString());
        }

        if (this.notificationService != null) {
            this.notificationService.notifyUpdateEBike(bike);
            this.notificationService.notifyEndRide(user, bike);
        }

        return true;
    }

    @Override
    public void attachPersistenceNotificationService(NotificationService persistenceNotificationService) {
        this.notificationService = persistenceNotificationService;
    }

    private void updatedBattery(EBike bike, int positionX, int positionY) {
        double distance;
        int battery;

        battery = bike.battery();
        distance = Point2D.distance(bike.positionX(), bike.positionY(), positionX, positionY);
        battery -= (int) (distance * BATTERY_CONSUMPTION_PER_METER);

        bike.setPositionX(positionX);
        bike.setPositionY(positionY);
        bike.setBattery(battery);

        if (battery <= 0) {
            bike.setState(EBikeState.OUT_OF_CHARGE.toString());
        }

    }

    private void updatedCredit(User user, long timeElapsed) {
        int credit;

        credit = user.credit();
        credit = credit - (int) (((double) timeElapsed / 1000) * CREDIT_CONSUMPTION_PER_SECOND);
        user.setCredit(credit);

    }

}


