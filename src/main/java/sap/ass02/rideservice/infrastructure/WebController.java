package sap.ass02.rideservice.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.rideservice.ClusterMembershipListenerImpl;
import sap.ass02.rideservice.ServiceLookupImpl;
import sap.ass02.rideservice.ServiceLookup;
import sap.ass02.rideservice.domain.entities.*;
import sap.ass02.rideservice.domain.ports.AppManager;
import sap.ass02.rideservice.domain.ports.ResourceRequest;
import sap.ass02.rideservice.utils.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.gui.utils.JsonFieldsConstants.CREDIT;
import static sap.ass02.gui.utils.JsonFieldsConstants.OPERATION;
import static sap.ass02.gui.utils.JsonFieldsConstants.RESULT;
import static sap.ass02.gui.utils.JsonFieldsConstants.USERNAME;
import static sap.ass02.gui.utils.JsonFieldsConstants.USER_ID;
import static sap.ass02.rideservice.utils.JsonFieldsConstants.*;

/**
 * The Vertx Server that handles all request
 * coming from clients.
 */
public class WebController extends AbstractVerticle implements ResourceRequest {

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String BIKE_CHANGE_EVENT_TOPIC = "ebike-Change";
    private static final String USER_CHANGE_EVENT_TOPIC = "users-Change";
    /**
     * The Vertx.
     */
    Vertx vertx;
    HazelcastInstance hazelcastInstance;
    final private AppManager pManager;
    final ServiceLookup serviceLookup;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(AppManager appManager) {
        this.port = 8080;
        LOGGER.setLevel(Level.FINE);
        this.pManager = appManager;
        this.serviceLookup = new ServiceLookupImpl();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","RideService");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8080");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new ClusterMembershipListenerImpl(this.serviceLookup)));
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                hazelcastInstance = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();
                serviceLookup.setVertxInstance(vertx);
                vertx.deployVerticle(this);
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "Web server initializing...");
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.POST, "/api/ride/command").handler(this::processServiceRideCmd);
        router.route(HttpMethod.GET, "/api/ride/query").handler(this::processServiceRideQuery);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena RideService web server ready on port: " + port);
        }

    }

    /**
     * Process a request that will need to WRITE
     * the Ride persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceRideCmd(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ride cmd " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            // Parse the JSON body
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                boolean b;
                switch (op) {
                    case CREATE:  {
                        if (requestBody.containsKey(USER_ID) && requestBody.containsKey(E_BIKE_ID)) {
                            int userId = requestBody.getInteger(USER_ID);
                            int eBikeId = requestBody.getInteger(E_BIKE_ID);
                            b = pManager.startRide(userId, eBikeId);
                            checkResponseAndSendReply(context, b);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case UPDATE:  {
                        if (requestBody.containsKey(USER_ID) && requestBody.containsKey(E_BIKE_ID) &&
                                requestBody.containsKey(POSITION_X) && requestBody.containsKey(POSITION_Y)) {
                            int userId = requestBody.getInteger(USER_ID);
                            int eBikeId = requestBody.getInteger(E_BIKE_ID);
                            int x = requestBody.getInteger(POSITION_X);
                            int y = requestBody.getInteger(POSITION_Y);
                            Pair<Integer, Integer> p = pManager.updateRide(userId, eBikeId, x, y);
                            var map = new HashMap<String, Object>();
                            map.put(CREDIT, p.first());
                            map.put(BATTERY, p.second());
                            composeJSONAndSendReply(context,map);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case DELETE:  {
                        if (requestBody.containsKey(USER_ID) && requestBody.containsKey(E_BIKE_ID)) {
                            int userId = requestBody.getInteger(USER_ID);
                            int eBikeId = requestBody.getInteger(E_BIKE_ID);
                            b = pManager.endRide(userId, eBikeId);
                            checkResponseAndSendReply(context, b);
                        } else if (requestBody.containsKey(RIDE_ID)) {
                            int rideId = requestBody.getInteger(RIDE_ID);
                            b = pManager.deleteRide(rideId);
                            checkResponseAndSendReply(context, b);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    default: invalidJSONReply(context,requestBody);
                }
            } else {
                invalidJSONReply(context,requestBody);
            }
        }).start();
    }

    /**
     * Process a request that will need to READ
     * the Ride persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceRideQuery(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ride query " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            // Parse the JSON body
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                Ride r;
                List<Ride> rides;
                if (Objects.requireNonNull(op) == WebOperation.READ) {
                    if (requestBody.containsKey("multiple")) {
                        if (requestBody.containsKey("ongoing")) {
                            boolean ongoing = requestBody.getBoolean("ongoing");
                            rides = pManager.getAllRides(ongoing,0,0);
                        } else if (requestBody.containsKey(USER_ID)) {
                            int userId = requestBody.getInteger(USER_ID);
                            rides = pManager.getAllRides(false,userId,0);
                        } else if (requestBody.containsKey(E_BIKE_ID)) {
                            int eBikeId = requestBody.getInteger(E_BIKE_ID);
                            rides = pManager.getAllRides(false,0,eBikeId);
                        } else {
                            rides = pManager.getAllRides(false,0,0);
                        }
                        var array = new ArrayList<Map<String,Object>>();
                        for (Ride ride : rides) {
                            var map = buildRideMap(ride);
                            array.add(map);
                        }
                        composeJSONArrayAndSendReply(context,array);
                    } else {
                        if (requestBody.containsKey(RIDE_ID)) {
                            int rideId = requestBody.getInteger(RIDE_ID);
                            r = pManager.getRide(rideId, 0);
                            var array = new ArrayList<Map<String,Object>>();
                            var map = buildRideMap(r);
                            array.add(map);
                            composeJSONArrayAndSendReply(context,array);
                        } else if (requestBody.containsKey(USER_ID)) {
                            int userId = requestBody.getInteger(USER_ID);
                            r = pManager.getRide(0, userId);
                            var array = new ArrayList<Map<String,Object>>();
                            var map = buildRideMap(r);
                            array.add(map);
                            composeJSONArrayAndSendReply(context,array);
                        } else {
                            invalidJSONReply(context, requestBody);
                        }
                    }
                } else {
                    invalidJSONReply(context,requestBody);
                }
            } else {
                invalidJSONReply(context,requestBody);
            }
        }).start();
    }

    private void checkResponseAndSendReply(RoutingContext context, boolean b) {
        JsonObject reply = new JsonObject();
        if (b) {
            reply.put(RESULT, "ok");
        } else {
            reply.put(RESULT, "error");
        }
        sendReply(context, reply);
    }

    private void composeJSONAndSendReply(RoutingContext context, Map<String,Object> body) {
        JsonObject reply = composeJSONFromFieldsMap(body);
        sendReply(context, reply);
    }

    private void composeJSONArrayAndSendReply(RoutingContext context, List<Map<String,Object>> body) {
        JsonObject reply = new JsonObject();
        JsonArray replyArray = new JsonArray();
        for (Map<String,Object> map : body) {
            JsonObject json = composeJSONFromFieldsMap(map);
            replyArray.add(json);
        }
        reply.put(RESULT, replyArray);
        sendReply(context, reply);
    }

    private JsonObject composeJSONFromFieldsMap(Map<String, Object> body) {
        JsonObject reply = new JsonObject();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String key = entry.getKey();
            reply.put(key, entry.getValue().toString());
        }
        return reply;
    }

    private void invalidJSONReply(RoutingContext context, JsonObject requestBody) {
        LOGGER.warning("Received invalid JSON payload: " + requestBody);
        JsonObject reply = new JsonObject();
        reply.put(RESULT, "not ok");
        sendReply(context, reply);
    }

    private Map<String, Object> buildRideMap(Ride r) {
        var map = new HashMap<String, Object>();
        map.put(RIDE_ID, r.id());
        map.put(USER_ID, r.userID());
        map.put(E_BIKE_ID, r.eBikeID());
        map.put("startDate", r.startDate());
        map.put("endDate", r.endDate() == null ? "" : r.endDate());
        return map;
    }

    private void notifyEBikeChanged(int eBikeId, int x, int y, int battery, String status) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "notify ebike changed");
        }
        EventBus eb = vertx.eventBus();

        JsonObject obj = new JsonObject();
        obj.put("event", BIKE_CHANGE_EVENT_TOPIC);
        obj.put(E_BIKE_ID, eBikeId);
        obj.put(POSITION_X, x);
        obj.put(POSITION_Y, y);
        obj.put(BATTERY, battery);
        obj.put("status", status);
        eb.publish(BIKE_CHANGE_EVENT_TOPIC, obj);
    }

    private void notifyUserChanged(int userId, int credit) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "notify user changed");
        }
        EventBus eb = vertx.eventBus();

        JsonObject obj = new JsonObject();
        obj.put("event", USER_CHANGE_EVENT_TOPIC);
        obj.put(USER_ID, userId);
        obj.put(CREDIT, credit);
        eb.publish(USER_CHANGE_EVENT_TOPIC, obj);
    }

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    @Override
    public Future<User> getUser(int id) {
        Promise<User> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, id);
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        Optional<WebClient> client = serviceLookup.getAPIGatewayClient();
        if (client.isPresent()) {
            client.get().post(USER_QUERY_PATH)
                    .sendJson(requestPayload, ar -> {
                        if (ar.succeeded()) {
                            JsonObject res = ar.result().bodyAsJsonObject();
                            if (res.containsKey(RESULT)) {
                                var resList = res.getJsonArray(RESULT);
                                var it = resList.stream().iterator();
                                var resUser = new UserImpl();
                                if (it.hasNext()) {
                                    var jsonObj = (JsonObject) it.next();
                                    int resId = Integer.parseInt(jsonObj.getString(USER_ID));
                                    resUser.setId(resId);
                                    resUser.setCredit(Integer.parseInt(jsonObj.getString(CREDIT)));
                                    resUser.setName(jsonObj.getString(USERNAME));
                                    resUser.setIsAdmin(Boolean.parseBoolean(jsonObj.getString("admin")));
                                }
                                promise.complete(resUser);
                            }
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
        return promise.future();
    }

    @Override
    public Future<EBike> getBike(int id) {
        Promise<EBike> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(E_BIKE_ID, id);
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        Optional<WebClient> client = serviceLookup.getAPIGatewayClient();
        if (client.isPresent()) {
            client.get().post(EBIKE_QUERY_PATH)
                    .sendJson(requestPayload, ar -> {
                        if (ar.succeeded()) {
                            JsonObject res = ar.result().bodyAsJsonObject();
                            if (res.containsKey(RESULT)) {
                                var resList = res.getJsonArray(RESULT);
                                var it = resList.stream().iterator();
                                var resBike = new EBikeImpl();
                                if (it.hasNext()) {
                                    var jsonObj = (JsonObject) it.next();
                                    int resId = Integer.parseInt(jsonObj.getString(E_BIKE_ID));
                                    resBike.setId(resId);
                                    resBike.setBattery(jsonObj.getInteger(BATTERY));
                                    resBike.setPositionX(jsonObj.getInteger(POSITION_X));
                                    resBike.setPositionY(jsonObj.getInteger(POSITION_Y));
                                    resBike.setState(jsonObj.getString("status"));
                                }
                                promise.complete(resBike);
                            }
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
        return promise.future();
    }

    @Override
    public void spreadUserChange(User user) {
        ITopic<String> topic = hazelcastInstance.getTopic("UserChangedFromRideService");
        JsonObject busPayload = new JsonObject();
        busPayload.put(USER_ID, user.id());
        busPayload.put(CREDIT, user.credit());

        topic.publish(busPayload.toString());
    }

    @Override
    public void spreadEBikeChange(EBike bike) {
        ITopic<String> topic = hazelcastInstance.getTopic("BikeChangedFromRideService");
        JsonObject busPayload = new JsonObject();
        busPayload.put(E_BIKE_ID, bike.id());
        busPayload.put(POSITION_X, bike.positionX());
        busPayload.put(POSITION_Y, bike.positionY());
        busPayload.put("status", bike.state());
        busPayload.put(BATTERY, bike.battery());

        topic.publish(busPayload.toString());
    }

    @Override
    public void spreadRideChanges(Ride ride, boolean start) {

    }
}
