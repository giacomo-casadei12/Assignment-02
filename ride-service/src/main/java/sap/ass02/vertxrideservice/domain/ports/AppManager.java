package sap.ass02.vertxrideservice.domain.ports;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import sap.ass02.vertxrideservice.domain.BusinessLogicL.NotificationService;
import sap.ass02.vertxrideservice.domain.BusinessLogicL.RideManager;
import sap.ass02.vertxrideservice.domain.BusinessLogicL.RideManagerImpl;
import sap.ass02.vertxrideservice.domain.entities.*;
import sap.ass02.vertxrideservice.utils.Pair;
import sap.ass02.vertxrideservice.utils.VertxSingleton;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.vertxrideservice.utils.JsonFieldsConstants.*;

/**
 * The implementation of the AppManager interface.
 */
public class AppManager extends AbstractVerticle implements NotificationService {

    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena RideService AppManager]");

    private final RideManager rideManager;
    private final Map<Pair<Integer, Integer>, Long> rideUpdateTimes = new ConcurrentHashMap<>();
    private final Map<Integer, JsonObject> usersRequested = new ConcurrentHashMap<>();
    private final Map<Integer, JsonObject> bikesRequested = new ConcurrentHashMap<>();
    private EventBus eventBus;

    /**
     * Instantiates a new App Manager
     *
     */
    public AppManager() {
        this.rideManager = new RideManagerImpl();
        this.rideManager.attachNotificationService(this);
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "App manager initializing...");
        this.eventBus = VertxSingleton.getInstance().getVertx().eventBus();
        eventBus.consumer("RideServiceGetAllRides", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getAllRides(Integer.parseInt(json.getString("RequestId")), Boolean.parseBoolean(json.getString("ongoing")),
                    Integer.parseInt(json.getString(USER_ID)), Integer.parseInt(json.getString(E_BIKE_ID)));
        });

        eventBus.consumer("RideServiceGetRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(RIDE_ID)), Integer.parseInt(json.getString(USER_ID)));
        });

        eventBus.consumer("RideServiceStopRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.stopRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(RIDE_ID)));
        });

        eventBus.consumer("RideServiceDeleteRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.deleteRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(RIDE_ID)));
        });

        eventBus.consumer("RideServiceStartRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.commandRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(USER_ID)), Integer.parseInt(json.getString(E_BIKE_ID)),
                    1, 0 ,0);
        });

        eventBus.consumer("RideServiceUpdateRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.commandRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(USER_ID)), Integer.parseInt(json.getString(E_BIKE_ID)), 2,
                    Integer.parseInt(json.getString(POSITION_X)), Integer.parseInt(json.getString(POSITION_Y)));
        });

        eventBus.consumer("RideServiceEndRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.commandRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(USER_ID)), Integer.parseInt(json.getString(E_BIKE_ID)),
                    3, 0, 0);
        });

        eventBus.consumer("RideServiceUpdateConfigurations", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            double batteryConsumption = Double.parseDouble(json.getString("BATTERY_CONSUMPTION_PER_METER"));
            double creditConsumption = Double.parseDouble(json.getString("CREDIT_CONSUMPTION_PER_SECOND"));
            this.rideManager.updateConfigurations(batteryConsumption,creditConsumption);
        });

        LOGGER.log(Level.INFO, "App manager ready to process messages");
    }

    private void getAllRides(int requestId, boolean ongoing, int userId, int eBikeId) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);
        if (ongoing) {
            eventBus.publish("RidePersistenceGetOngoingRides", busPayload.toString());
        } else if (userId > 0) {
            busPayload.put(USER_ID, userId);
            eventBus.publish("RidePersistenceGetAllRidesByUser", busPayload.toString());
        } else if (eBikeId > 0) {
            busPayload.put(E_BIKE_ID, eBikeId);
            eventBus.publish("RidePersistenceGetAllRidesByBike", busPayload.toString());
        } else {
            eventBus.publish("RidePersistenceGetAllRides", busPayload.toString());
        }
    }

    private void getRide(int requestId, int rideId, int userId) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);
        if (rideId != 0 && userId == 0) {
            busPayload.put(RIDE_ID, rideId);
            eventBus.publish("RidePersistenceGetRideByRideId", busPayload.toString());
        } else {
            busPayload.put(USER_ID, userId);
            eventBus.publish("RidePersistenceGetRideByUser", busPayload.toString());
        }
    }

    private void stopRide(int requestId, int id) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);
        busPayload.put(RIDE_ID, id);
        eventBus.publish("RidePersistenceEndRide", busPayload.toString());
    }

    private void deleteRide(int requestId, int id) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);
        busPayload.put(RIDE_ID, id);
        eventBus.publish("RidePersistenceDeleteRide", busPayload.toString());
    }

    private void commandRide(int requestId, int userID, int bikeID, int mode,
                             int positionX, int positionY) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);
        busPayload.put(E_BIKE_ID, bikeID);
        eventBus.publish("RideWebControllerGetBike", busPayload.toString());
        busPayload.remove(E_BIKE_ID);
        busPayload.put(USER_ID, userID);
        eventBus.publish("RideWebControllerGetUser", busPayload.toString());

        String userAddress = "RideServiceGetUser" + requestId;
        eventBus.consumer(userAddress, msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            int userRequestId = Integer.parseInt(json.getString("RequestId"));
            this.usersRequested.put(userRequestId,json);

            if (!Objects.isNull(this.bikesRequested.get(userRequestId))) {
                switch (mode) {
                    case 1:
                        this.startRide(userRequestId);
                        break;
                    case 2:
                        this.updateRide(userRequestId, positionX, positionY);
                        break;
                    case 3:
                        this.endRide(userRequestId);
                        break;
                    default:
                }
            }
        });

        String bikeAddress = "RideServiceGetBike" + requestId;
        eventBus.consumer(bikeAddress, msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            int bikeRequestId = Integer.parseInt(json.getString("RequestId"));
            this.bikesRequested.put(bikeRequestId,json);

            if (!Objects.isNull(this.usersRequested.get(bikeRequestId))) {
                switch (mode) {
                    case 1:
                        this.startRide(bikeRequestId);
                        break;
                    case 2:
                        this.updateRide(bikeRequestId, positionX, positionY);
                        break;
                    case 3:
                        this.endRide(bikeRequestId);
                        break;
                    default:
                }
            }
        });
    }

    private void startRide(int requestId) {
        var now = new Date().getTime();
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);

        JsonObject userJson = this.usersRequested.remove(requestId);
        JsonObject bikeJson = this.bikesRequested.remove(requestId);

        User user = buildUserFromJson(userJson);
        EBike eBike = buildBikeFromJson(bikeJson);

        rideUpdateTimes.put(new Pair<>(user.id(),eBike.id()), now);
        boolean success = this.rideManager.startRide(user,eBike);
        if (success) {
            busPayload.put(USER_ID, user.id());
            busPayload.put(E_BIKE_ID, eBike.id());
            eventBus.publish("RidePersistenceCreateRide", busPayload.toString());
        }

    }

    public void updateRide(int requestId, int x, int y) {
        var now = new Date().getTime();
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", requestId);

        JsonObject userJson = this.usersRequested.remove(requestId);
        JsonObject bikeJson = this.bikesRequested.remove(requestId);

        User user = buildUserFromJson(userJson);
        EBike eBike = buildBikeFromJson(bikeJson);

        long last = rideUpdateTimes.get(new Pair<>(user.id(), eBike.id()));
        long timeElapsed = now - last;

        rideUpdateTimes.put(new Pair<>(user.id(), eBike.id()), now);

        var res = this.rideManager.updateRide(user, eBike, x, y, timeElapsed);

        busPayload.put(CREDIT, res.first());
        busPayload.put(BATTERY, res.second());
        eventBus.publish("RideWebControllerSendResponse", busPayload.toString());
    }

    public void endRide(int requestId) {

        JsonObject userJson = this.usersRequested.remove(requestId);
        JsonObject bikeJson = this.bikesRequested.remove(requestId);

        User user = buildUserFromJson(userJson);
        EBike eBike = buildBikeFromJson(bikeJson);

        this.rideManager.endRide(user,eBike);
    }

    private User buildUserFromJson(JsonObject json) {
        User user = new UserImpl();
        user.setId(Integer.parseInt(json.getString(USER_ID)));
        user.setCredit(Integer.parseInt(json.getString(CREDIT)));
        user.setName(json.getString(USERNAME));
        user.setIsAdmin(Boolean.parseBoolean(json.getString(ADMIN)));

        return user;
    }

    private EBike buildBikeFromJson(JsonObject json) {
        EBike bike = new EBikeImpl();
        bike.setId(Integer.parseInt(json.getString(E_BIKE_ID)));
        bike.setBattery(Integer.parseInt(json.getString(BATTERY)));
        bike.setPositionX(Integer.parseInt(json.getString(POSITION_X)));
        bike.setPositionY(Integer.parseInt(json.getString(POSITION_Y)));
        bike.setState(json.getString("status"));

        return bike;
    }

    public void notifyUpdateUser(User user) {
        JsonObject busPayload = new JsonObject();
        busPayload.put(USER_ID, user.id());
        busPayload.put(CREDIT, user.credit());
        eventBus.publish("RideWebControllerSpreadUserChange", busPayload.toString());
    }

    public void notifyUpdateEBike(EBike bike) {
        JsonObject busPayload = new JsonObject();
        busPayload.put(E_BIKE_ID, bike.id());
        busPayload.put(POSITION_X, bike.positionX());
        busPayload.put(POSITION_Y, bike.positionY());
        busPayload.put("status", bike.state());
        busPayload.put(BATTERY, bike.battery());
        eventBus.publish("RideWebControllerSpreadBikeChange", busPayload.toString());
    }

    public void notifyEndRide(User user, EBike bike) {
        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 0);
        busPayload.put(USER_ID, user.id());
        eventBus.publish("RidePersistenceEndRide", busPayload.toString());
        //this.resourceRequest.spreadRideChanges(ride,false);
    }

}
