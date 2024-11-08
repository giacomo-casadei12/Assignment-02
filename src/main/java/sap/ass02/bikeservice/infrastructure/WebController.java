package sap.ass02.bikeservice.infrastructure;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.bikeservice.domain.entities.EBike;
import sap.ass02.bikeservice.domain.ports.AppManager;
import sap.ass02.bikeservice.utils.EBikeState;
import sap.ass02.bikeservice.utils.WebOperation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.rideservice.utils.JsonFieldsConstants.*;

/**
 * The Vertx Server that handles all request
 * coming from clients.
 */
public class WebController extends AbstractVerticle {

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private static final String BIKE_CHANGE_EVENT_TOPIC = "ebike-Change";
    /**
     * The Vertx.
     */
    Vertx vertx;
    final private AppManager pManager;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(AppManager appManager) {
        this.port = 8082;
        LOGGER.setLevel(Level.FINE);
        this.pManager = appManager;
        Config hazelcastConfig = new Config();

        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
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

        router.route(HttpMethod.POST, "/api/ebike/command").handler(this::processServiceEBikeCmd);
        router.route(HttpMethod.GET, "/api/ebike/query").handler(this::processServiceEBikeQuery);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena web server ready on port: " + port);
        }

    }

    /**
     * Process a request that will need to WRITE
     * the EBike persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceEBikeCmd(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ebike cmd " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            // Parse the JSON body
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                boolean b = false;
                switch (op) {
                    case CREATE:  {
                        if (requestBody.containsKey(POSITION_X) && requestBody.containsKey(POSITION_Y)) {
                            int x = requestBody.getInteger(POSITION_X);
                            int y = requestBody.getInteger(POSITION_Y);
                            b = pManager.createEBike(x, y);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case UPDATE:  {
                        if (requestBody.containsKey(E_BIKE_ID) && requestBody.containsKey(POSITION_Y) && requestBody.containsKey(POSITION_Y) && !requestBody.containsKey(BATTERY)) {
                            int id = requestBody.getInteger(E_BIKE_ID);
                            int x = requestBody.getInteger(POSITION_X);
                            int y = requestBody.getInteger(POSITION_Y);
                            b = pManager.updateEbikePosition(id,x,y);
                        } else if (requestBody.containsKey(E_BIKE_ID) && requestBody.containsKey(BATTERY) && requestBody.containsKey(STATE)) {
                            int id = requestBody.getInteger(E_BIKE_ID);
                            int battery = requestBody.getInteger(BATTERY);
                            EBikeState state = EBikeState.valueOf(requestBody.getString(STATE));
                            int x = requestBody.getInteger(POSITION_X);
                            int y = requestBody.getInteger(POSITION_Y);
                            b = pManager.updateEBike(id,battery,state, x, y);
                            if (b) {
                                notifyEBikeChanged(id, x, y, battery, state.toString());
                            }
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case DELETE:  {
                        if (requestBody.containsKey(E_BIKE_ID)) {
                            int id = requestBody.getInteger(E_BIKE_ID);
                            b = pManager.deleteEBike(id);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    default: invalidJSONReply(context,requestBody);
                }
                checkResponseAndSendReply(context, b);
                /*notifyEBikeChanged(newCount);*/
            } else {
                invalidJSONReply(context,requestBody);
            }
        }).start();
    }

    /**
     * Process a request that will need to READ
     * the EBike persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceEBikeQuery(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - ebike query " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            // Parse the JSON body
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                EBike eb;
                List<EBike> bikes;
                if (Objects.requireNonNull(op) == WebOperation.READ) {
                    if (requestBody.containsKey(E_BIKE_ID)) {
                        int id = requestBody.getInteger(E_BIKE_ID);
                        eb = pManager.getEBike(id);
                        var map = buildEBikeMap(eb);
                        composeJSONAndSendReply(context,map);
                    } else {
                        if (requestBody.containsKey(POSITION_X) || requestBody.containsKey(POSITION_Y)) {
                            int x = requestBody.containsKey(POSITION_X) ? requestBody.getInteger(POSITION_X) : 0;
                            int y = requestBody.containsKey(POSITION_Y) ? requestBody.getInteger(POSITION_Y) : 0;
                            bikes = pManager.getAllEBikes(x,y,false);
                        } else if (requestBody.containsKey("available")) {
                            boolean avail = requestBody.getBoolean("available");
                            bikes = pManager.getAllEBikes(0,0,avail);
                        } else {
                            bikes = pManager.getAllEBikes(0,0,false);
                        }
                        var array = new ArrayList<Map<String,Object>>();
                        for (EBike eBike : bikes) {
                            var map = buildEBikeMap(eBike);
                            array.add(map);
                        }
                        composeJSONArrayAndSendReply(context,array);
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

    private Map<String, Object> buildEBikeMap(EBike eb) {
        var map = new HashMap<String, Object>();
        map.put(E_BIKE_ID, eb.id());
        map.put(POSITION_X, eb.positionX());
        map.put(POSITION_Y, eb.positionY());
        map.put(BATTERY, eb.battery());
        map.put("status", eb.state());
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

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
