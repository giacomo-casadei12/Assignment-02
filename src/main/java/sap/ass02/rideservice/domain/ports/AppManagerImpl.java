package sap.ass02.rideservice.domain.ports;

import sap.ass02.rideservice.domain.BusinessLogicL.NotificationService;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManager;
import sap.ass02.rideservice.domain.entities.*;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.RideDA;
import sap.ass02.rideservice.utils.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of the AppManager interface.
 */
public class AppManagerImpl implements AppManager, NotificationService {

    private final RideDA rideDA;
    private final RideManager rideManager;
    private ResourceRequest resourceRequest;
    private final Map<Pair<Integer, Integer>, Long> rideUpdateTimes = new ConcurrentHashMap<>();

    /**
     * Instantiates a new App Manager
     *
     * @param rideManager the logic for handle rides
     * @param rideDA      the persistence abstraction for rides
     */
    public AppManagerImpl(RideManager rideManager, RideDA rideDA) {
        this.rideDA = rideDA;
        this.rideManager = rideManager;
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
    public boolean endRide(int id) {
        return this.rideDA.endRide(id);
    }

    @Override
    public boolean deleteRide(int id) {
        return this.rideDA.deleteRide(id);
    }

    @Override
    public boolean startRide(int userID, int bikeID) {
        var now = new Date().getTime();
        User user = new UserImpl();
        EBike eBike = new EBikeImpl();
        CompletableFuture<EBike> cfb = this.resourceRequest.getBike(bikeID).toCompletionStage().toCompletableFuture();
        CompletableFuture<User> cfu = this.resourceRequest.getUser(userID).toCompletionStage().toCompletableFuture();
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfb, cfu);
        cf.join();
        try  {
            eBike = cfb.get();
            user = cfu.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rideUpdateTimes.put(new Pair<>(user.id(), eBike.id()), now);
        boolean success = this.rideManager.startRide(user,eBike);
        if (success) {
            success = this.rideDA.createRide(userID, bikeID);
        }
        return success;
    }

    @Override
    public Pair<Integer, Integer> updateRide(int userID, int bikeID, int x, int y) {
        var now = new Date().getTime();
        User user = new UserImpl();
        EBike eBike = new EBikeImpl();
        CompletableFuture<EBike> cfb = this.resourceRequest.getBike(bikeID).toCompletionStage().toCompletableFuture();
        CompletableFuture<User> cfu = this.resourceRequest.getUser(userID).toCompletionStage().toCompletableFuture();
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfb, cfu);
        cf.join();
        try  {
            eBike = cfb.get();
            user = cfu.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long last = rideUpdateTimes.get(new Pair<>(user.id(), eBike.id()));
        long timeElapsed = now - last;
        rideUpdateTimes.put(new Pair<>(user.id(), eBike.id()), now);
        return this.rideManager.updateRide(user, eBike, x, y, timeElapsed);
    }

    @Override
    public boolean endRide(int userID, int bikeID) {
        User user = new UserImpl();
        EBike eBike = new EBikeImpl();
        CompletableFuture<EBike> cfb = this.resourceRequest.getBike(bikeID).toCompletionStage().toCompletableFuture();
        CompletableFuture<User> cfu = this.resourceRequest.getUser(userID).toCompletionStage().toCompletableFuture();
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfb, cfu);
        cf.join();
        try  {
            eBike = cfb.get();
            user = cfu.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.rideManager.endRide(user,eBike);
    }

    @Override
    public void attachResourceRequest(ResourceRequest resourceRequest) {
        this.resourceRequest = resourceRequest;
    }


    @Override
    public void notifyUpdateUser(User user) {
        this.resourceRequest.spreadUserChange(user);
    }

    @Override
    public void notifyUpdateEBike(EBike bike) {
        this.resourceRequest.spreadEBikeChange(bike);
    }


    @Override
    public void notifyEndRide(User user, EBike bike) {
        Ride ride = this.getRide(0, user.id());
        this.endRide(ride.id());
        this.resourceRequest.spreadRideChanges(ride,false);
    }

}
