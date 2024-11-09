package sap.ass02.rideservice.domain.ports;

import sap.ass02.rideservice.domain.BusinessLogicL.PersistenceNotificationService;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManager;
import sap.ass02.rideservice.domain.entities.*;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.RideDA;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.UserDA;
import sap.ass02.rideservice.utils.EBikeState;
import sap.ass02.rideservice.utils.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of the AppManager interface.
 */
public class AppManagerImpl implements AppManager, PersistenceNotificationService {

    private final EBikeDA bikeDA;
    private final RideDA rideDA;
    private final UserDA userDA;
    private final RideManager rideManager;
    private final Map<Pair<Integer, Integer>, Long> rideUpdateTimes = new ConcurrentHashMap<>();

    /**
     * Instantiates a new App Manager
     *
     * @param rideManager the logic for handle rides
     * @param rideDA      the persistence abstraction for rides
     * @param bikeDA      the persistence abstraction for bikes
     * @param userDA      the persistence abstraction for users
     */
    public AppManagerImpl(RideManager rideManager, RideDA rideDA, EBikeDA bikeDA, UserDA userDA) {
        this.bikeDA = bikeDA;
        this.rideDA = rideDA;
        this.userDA = userDA;
        this.rideManager = rideManager;
    }

    @Override
    public EBike getEBike(int id) {
        return this.bikeDA.getEBikeById(id);
    }

    @Override
    public List<Ride> getAllRides(boolean ongoing, int userId, int eBikeId) {
        List<Ride> res;

        if (ongoing) {
            res = this.rideDA.getAllOngoingRides();
        } else if (userId > 0) {
            res = this.rideDA.getAllRidesByUser(userId);
        } else if (eBikeId > 0) {
            res = this.rideDA.getAllRidesByEBike(eBikeId);
        } else {
            res = this.rideDA.getAllRides();
        }

        return res;
    }

    @Override
    public Ride getRide(int rideId, int userId) {
        return rideId != 0 && userId == 0 ?
                this.rideDA.getRideById(rideId) :
                this.rideDA.getOngoingRideByUserId(userId);
    }

    @Override
    public boolean updateUser(int id, int credit) {
        return this.userDA.updateUser(id, credit);
    }

    @Override
    public boolean updateEBike(int id, int battery, EBikeState state, int positionX, int positionY) {
        EBike bike = new EBikeImpl();
        bike.setBattery(battery);
        bike.setId(id);
        bike.setPositionX(positionX);
        bike.setPositionY(positionY);
        bike.setState(state.toString());
        return this.bikeDA.updateEBike(bike);
    }

    @Override
    public boolean endRide(int id) {
        return this.rideDA.endRide(id);
    }

    @Override
    public boolean deleteRide(int id) {
        return this.rideDA.deleteRide(id);
    }

    @Override
    public void notifyUpdateUser(User user) {
        this.updateUser(user.id(), user.credit());
    }

    @Override
    public void notifyUpdateEBike(EBike bike) {
        this.updateEBike(bike.id(), bike.battery(), EBikeState.valueOf(bike.state()),
                bike.positionX(), bike.positionY());
    }

    @Override
    public void notifyEndRide(User user, EBike bike) {
        Ride ride = this.getRide(0, user.id());
        this.endRide(ride.id());
    }

    @Override
    public boolean startRide(int userID, int bikeID) {
        User user = this.userDA.getUserById(userID);
        EBike eBike = this.bikeDA.getEBikeById(bikeID);
        var now = new Date().getTime();
        rideUpdateTimes.put(new Pair<>(user.id(), eBike.id()), now);
        boolean success = this.rideManager.startRide(user,eBike);
        if (success) {
            success = this.rideDA.createRide(userID, bikeID);
        }
        return success;
    }

    @Override
    public Pair<Integer, Integer> updateRide(int userID, int bikeID, int x, int y) {
        User user = this.userDA.getUserById(userID);
        EBike eBike = this.bikeDA.getEBikeById(bikeID);
        var now = new Date().getTime();
        long last = rideUpdateTimes.get(new Pair<>(user.id(), eBike.id()));
        long timeElapsed = now - last;
        rideUpdateTimes.put(new Pair<>(user.id(), eBike.id()), now);
        return this.rideManager.updateRide(user, eBike, x, y, timeElapsed);
    }

    @Override
    public boolean endRide(int userID, int bikeID) {
        User user = this.userDA.getUserById(userID);
        EBike eBike = this.bikeDA.getEBikeById(bikeID);
        return this.rideManager.endRide(user,eBike);
    }
}
