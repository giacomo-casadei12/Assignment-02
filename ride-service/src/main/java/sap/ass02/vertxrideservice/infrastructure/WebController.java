package sap.ass02.vertxrideservice.infrastructure;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.vertxrideservice.utils.VertxSingleton;
import sap.ass02.vertxrideservice.utils.WebOperation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.vertxrideservice.utils.JsonFieldsConstants.*;

/**
 * The Vertx Server that handles all request
 * coming from clients.
 */
public class WebController extends AbstractVerticle {

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena RideService WebController]");
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String CONFIGURATION_QUERY_PATH = "/api/configuration/query";
    private static final String HEALTH_CHECK_PATH = "/healthCheck";

    private final Map<Integer, RoutingContext> routingContexts = new ConcurrentHashMap<>();
    private final Map<Integer, Timer> timers = new ConcurrentHashMap<>();

    private final Histogram http_request_duration_histogram;
    private final Counter requests_counter;

    private int requestCounter = 1;
    /**
     * The Vertx.
     */
    Vertx vertx;
    final ServiceLookup serviceLookup;
    EventBus eventBus;
    private HazelcastClusterManager clusterManager;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(ServiceLookup serviceLookup) {
        this.port = 8080;
        LOGGER.setLevel(Level.FINE);
        this.serviceLookup = serviceLookup;

        http_request_duration_histogram = Histogram.builder()
                .name("http_request_duration_seconds")
                .help("Histogram of http request durations in seconds")
                .register();

        requests_counter = Counter.builder()
                .name("request_counter")
                .help("counter of incoming request")
                .register();
    }

    public void attachClusterManager(HazelcastClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void start() {

        try {
            HTTPServer.builder()
                    .port(this.port+100)
                    .buildAndStart();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.log(Level.INFO, "Web server initializing...");
        this.vertx = VertxSingleton.getInstance().getVertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.POST, "/api/ride/command").handler(this::processServiceRideCmd);
        router.route(HttpMethod.GET, "/api/ride/query").handler(this::processServiceRideQuery);

        router.route(HttpMethod.GET, HEALTH_CHECK_PATH).handler(this::healthCheckHandler);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena RideService web server ready on port: " + port);
        }

        HazelcastInstance hz = clusterManager.getHazelcastInstance();

        this.eventBus = vertx.eventBus();

        IMap<String, String> map = hz.getMap("configurations");
        map.addEntryListener(new ClusterEntryListener(this.eventBus), true);

        eventBus.consumer("RideWebControllerSendResponse", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            Integer requestId = Integer.parseInt(json.getString("RequestId"));
            this.sendReply(Integer.parseInt(json.remove("RequestId").toString()),
                    json);

            Timer timer = this.timers.remove(requestId);
            timer.observeDuration();
            timer.close();
        });

        eventBus.consumer("RideWebControllerGetBike", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getBike(Integer.parseInt(json.remove("RequestId").toString()),
                    Integer.parseInt(json.remove(E_BIKE_ID).toString()));
        });

        eventBus.consumer("RideWebControllerGetUser", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getUser(Integer.parseInt(json.remove("RequestId").toString()),
                    Integer.parseInt(json.remove(USER_ID).toString()));
        });

        eventBus.consumer("RideWebControllerSpreadBikeChange", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            vertx.eventBus().publish("BikeChangedFromRideService", json.toString());
        });

        eventBus.consumer("RideWebControllerSpreadUserChange", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            vertx.eventBus().publish("UserChangedFromRideService", json.toString());
        });

        eventBus.consumer("RideWebControllerSpreadUserChange", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            vertx.eventBus().publish("UserChangedFromRideService", json.toString());
        });

        eventBus.consumer("RideWebControllerConfigurationsChanged", _ -> this.getConfiguration());

        this.getConfiguration();
    }

    private void getConfiguration() {
        JsonObject requestPayload = new JsonObject();
        requestPayload.put("requestedConfig", "rideService");

        Optional<WebClient> client = serviceLookup.getConfigurationServerClient();
        if (client.isPresent()) {
            client.get().get(CONFIGURATION_QUERY_PATH)
                    .sendJson(requestPayload, ar -> {
                        if (ar.succeeded()) {
                            if (!ar.result().bodyAsJsonObject().containsKey("Error")) {
                                JsonObject res = ar.result().bodyAsJsonObject();
                                eventBus.publish("RideServiceUpdateConfigurations", res);
                            } else {
                                LOGGER.severe(ar.result().bodyAsJsonObject().getString("Error"));
                            }
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Configuration Server not found");
        }

    }

    /**
     * Process a request that will need to WRITE
     * the Ride persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceRideCmd(RoutingContext context) {
        requests_counter.inc();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ride cmd " + context.currentRoute().getPath());
        }
        int requestId = requestCounter;
        requestCounter++;
        this.routingContexts.put(requestId, context);
        Timer timer = http_request_duration_histogram.startTimer();
        timers.put(requestId, timer);
        JsonObject requestBody = context.body().asJsonObject();
        if (requestBody != null && requestBody.containsKey(OPERATION)) {
            WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
            switch (op) {
                case CREATE:  {
                    if (requestBody.containsKey(USER_ID) && requestBody.containsKey(E_BIKE_ID)) {
                        int userId = requestBody.getInteger(USER_ID);
                        int eBikeId = requestBody.getInteger(E_BIKE_ID);

                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, eBikeId);
                        busPayload.put(USER_ID, userId);
                        eventBus.publish("RideServiceStartRide", busPayload.toString());
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
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, eBikeId);
                        busPayload.put(USER_ID, userId);
                        busPayload.put(POSITION_X, x);
                        busPayload.put(POSITION_Y, y);
                        eventBus.publish("RideServiceUpdateRide", busPayload.toString());
                    } else {
                        invalidJSONReply(context,requestBody);
                    }
                    break;
                }
                case DELETE:  {
                    if (requestBody.containsKey(USER_ID) && requestBody.containsKey(E_BIKE_ID)) {
                        int userId = requestBody.getInteger(USER_ID);
                        int eBikeId = requestBody.getInteger(E_BIKE_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, eBikeId);
                        busPayload.put(USER_ID, userId);
                        eventBus.publish("RideServiceEndRide", busPayload.toString());
                    } else if (requestBody.containsKey(RIDE_ID)) {
                        int rideId = requestBody.getInteger(RIDE_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(RIDE_ID, rideId);
                        eventBus.publish("RideServiceDeleteRide", busPayload.toString());
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
    }

    /**
     * Process a request that will need to READ
     * the Ride persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceRideQuery(RoutingContext context) {
        requests_counter.inc();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ride query " + context.currentRoute().getPath());
        }
        int requestId = requestCounter;
        requestCounter++;
        this.routingContexts.put(requestId, context);
        Timer timer = http_request_duration_histogram.startTimer();
        timers.put(requestId, timer);
        // Parse the JSON body
        JsonObject requestBody = context.body().asJsonObject();
        if (requestBody != null && requestBody.containsKey(OPERATION)) {
            WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
            if (Objects.requireNonNull(op) == WebOperation.READ) {
                if (requestBody.containsKey("multiple")) {
                    if (requestBody.containsKey("ongoing")) {
                        boolean ongoing = requestBody.getBoolean("ongoing");
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, 0);
                        busPayload.put(USER_ID, 0);
                        busPayload.put("ongoing", ongoing);
                        eventBus.publish("RideServiceGetAllRides", busPayload.toString());
                    } else if (requestBody.containsKey(USER_ID)) {
                        int userId = requestBody.getInteger(USER_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, 0);
                        busPayload.put(USER_ID, userId);
                        busPayload.put("ongoing", false);
                        eventBus.publish("RideServiceGetAllRides", busPayload.toString());
                    } else if (requestBody.containsKey(E_BIKE_ID)) {
                        int eBikeId = requestBody.getInteger(E_BIKE_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, eBikeId);
                        busPayload.put(USER_ID, 0);
                        busPayload.put("ongoing", false);
                        eventBus.publish("RideServiceGetAllRides", busPayload.toString());
                    } else {
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(E_BIKE_ID, 0);
                        busPayload.put(USER_ID, 0);
                        busPayload.put("ongoing", false);
                        eventBus.publish("RideServiceGetAllRides", busPayload.toString());
                    }
                } else {
                    if (requestBody.containsKey(RIDE_ID)) {
                        int rideId = requestBody.getInteger(RIDE_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(RIDE_ID, rideId);
                        busPayload.put(USER_ID, 0);
                        eventBus.publish("RideServiceGetRide", busPayload.toString());
                    } else if (requestBody.containsKey(USER_ID)) {
                        int userId = requestBody.getInteger(USER_ID);
                        JsonObject busPayload = new JsonObject();
                        busPayload.put("RequestId", requestId);
                        busPayload.put(RIDE_ID, 0);
                        busPayload.put(USER_ID, userId);
                        eventBus.publish("RideServiceGetRide", busPayload.toString());
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
    }

    protected void healthCheckHandler(RoutingContext context) {
        LOGGER.log(Level.INFO, "Health check request " + context.currentRoute().getPath());
        JsonObject reply = new JsonObject();
        reply.put("status", "UP");
        JsonArray checks = new JsonArray();

        eventBus.consumer("RideServiceHealthCheck", _ -> {
            //check eventbus liveness
            checks.add(true);
            reply.put("checks", checks);
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.end(reply.toString());
        });

        eventBus.publish("RideServiceHealthCheck", reply.toString());
    }

    private void invalidJSONReply(RoutingContext context, JsonObject requestBody) {
        LOGGER.warning("Received invalid JSON payload: " + requestBody);
        JsonObject reply = new JsonObject();
        reply.put(RESULT, "not ok");
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    private void sendReply(int requestId, JsonObject reply) {
        RoutingContext request = this.routingContexts.remove(requestId);
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    public void getUser(int requestId, int id) {
        String returnAddress = "RideServiceGetUser" + requestId;
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, id);
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        Optional<WebClient> client = serviceLookup.getAPIGatewayClient();
        if (client.isPresent()) {
            client.get().get(USER_QUERY_PATH)
                    .sendJson(requestPayload, ar -> {
                        if (ar.succeeded()) {
                            JsonObject res = ar.result().bodyAsJsonObject();
                            res.put("RequestId", requestId);
                            eventBus.publish(returnAddress, res.toString());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }

    }

    public void getBike(int requestId, int id) {
        String returnAddress = "RideServiceGetBike" + requestId;
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(E_BIKE_ID, id);
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        Optional<WebClient> client = serviceLookup.getAPIGatewayClient();
        if (client.isPresent()) {
            client.get().get(EBIKE_QUERY_PATH)
                    .sendJson(requestPayload, ar -> {
                        if (ar.succeeded()) {
                            JsonObject res = ar.result().bodyAsJsonObject();
                            if (res.containsKey(E_BIKE_ID) && res.containsKey(BATTERY) &&
                                    res.containsKey(POSITION_X) && res.containsKey(POSITION_Y) &&
                                    res.containsKey("status")) {
                                res.put("RequestId", requestId);
                                eventBus.publish(returnAddress, res.toString());
                            }
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

}
