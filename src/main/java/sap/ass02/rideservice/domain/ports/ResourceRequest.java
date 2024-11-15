package sap.ass02.rideservice.domain.ports;

import io.vertx.core.Future;
import sap.ass02.rideservice.domain.entities.EBike;
import sap.ass02.rideservice.domain.entities.Ride;
import sap.ass02.rideservice.domain.entities.User;

public interface ResourceRequest {

    Future<User> getUser(int id);

    Future<EBike> getBike(int id);

    void spreadUserChange(User user);

    void spreadEBikeChange(EBike bike);

    void spreadRideChanges(Ride ride, boolean start);

}
