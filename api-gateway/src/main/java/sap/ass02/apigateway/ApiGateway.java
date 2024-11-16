package sap.ass02.apigateway;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.apigateway.utils.WebOperation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.apigateway.utils.JsonFieldsConstants.*;

public class ApiGateway extends AbstractVerticle {

    private static final String USER_COMMAND_PATH = "/api/user/command";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String EBIKE_COMMAND_PATH = "/api/ebike/command";
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String RIDE_COMMAND_PATH = "/api/ride/command";
    private static final String RIDE_QUERY_PATH = "/api/ride/query";
    private static final String SERVICE_COMMAND_PATH = "/api/service/command";

    private static final String BIKE_CHANGE_EVENT_TOPIC = "ebike-Change";
    private static final String USER_CHANGE_EVENT_TOPIC = "users-Change";

    private final ServiceLookup serviceLookup;

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private Vertx vertx;
    HazelcastInstance hazelcastInstance;

    public ApiGateway() {
        this.port = 8085;
        LOGGER.setLevel(Level.FINE);
        this.serviceLookup = new ServiceLookupImpl();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","ApiGateway");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8085");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new ClusterMembershipListenerImpl(this.serviceLookup)));
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions vOptions = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(vOptions, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                hazelcastInstance = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();
                serviceLookup.setVertxInstance(vertx);
                LOGGER.setLevel(Level.FINE);
                vertx.deployVerticle(this);
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "Api Gateway initializing...");
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.POST, EBIKE_COMMAND_PATH).handler(this::processServiceEBikeCmd);
        router.route(HttpMethod.GET, EBIKE_QUERY_PATH).handler(this::processServiceEBikeQuery);
        router.route(HttpMethod.POST, USER_COMMAND_PATH).handler(this::processServiceUserCmd);
        router.route(HttpMethod.GET, USER_QUERY_PATH).handler(this::processServiceUserQuery);
        router.route(HttpMethod.POST, RIDE_COMMAND_PATH).handler(this::processServiceRideCmd);
        router.route(HttpMethod.GET, RIDE_QUERY_PATH).handler(this::processServiceRideQuery);

        router.route(HttpMethod.POST, SERVICE_COMMAND_PATH).handler(this::processServiceCmd);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena Api Gateway ready on port: " + port);
        }

        ITopic<String> topic = hazelcastInstance.getTopic("Grodus");
        topic.addMessageListener(message -> {
            /*String jsonString = message.getMessageObject();
            JSONObject json = new JSONObject(jsonString);

            // Extract values from the JSONObject
            String name = json.getString("name");
            int age = json.getInt("age");
*/
            // Process the received data
            System.out.println("Received: Groda");
        });
    }

    protected void processServiceEBikeCmd(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getBikeClient();
        if (client.isPresent()) {
            client.get().post(EBIKE_COMMAND_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceEBikeQuery(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getBikeClient();
        if (client.isPresent()) {
            client.get().get(EBIKE_QUERY_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceRideCmd(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getRideClient();
        if (client.isPresent()) {
            client.get().post(RIDE_COMMAND_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceRideQuery(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getRideClient();
        if (client.isPresent()) {
            client.get().get(RIDE_QUERY_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceUserCmd(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getUserClient();
        if (client.isPresent()) {
            client.get().post(USER_COMMAND_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceUserQuery(RoutingContext context) {
        Optional<WebClient> client = serviceLookup.getUserClient();
        if (client.isPresent()) {
            client.get().get(USER_QUERY_PATH)
                    .sendJson(context.body().asJsonObject(), ar -> {
                        if (ar.succeeded()) {
                            sendReply(context, ar.result().bodyAsJsonObject());
                        } else {
                            LOGGER.severe(ar.cause().getMessage());
                        }
                    });
        } else {
            System.out.println("EBikeCesena Api Gateway client not found");
        }
    }

    protected void processServiceCmd(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New service request " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                boolean b = false;
                switch (op) {
                    case CREATE:  {
                        if (requestBody.containsKey(SERVICE_NAME) && requestBody.containsKey(SERVICE_PORT) &&
                            requestBody.containsKey(SERVICE_ADDRESS)) {
                            checkServiceAndPlugIt(requestBody);
                            b = true;
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case UPDATE:  {
                        if (requestBody.containsKey(SERVICE_NAME) && requestBody.containsKey(TOPIC)) {
                            checkServiceAndHandleTopic(requestBody);
                            b = true;
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case DELETE:  {
                        if (requestBody.containsKey(SERVICE_NAME)) {
                            checkServiceAndUnplugIt(requestBody);
                            b = true;
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    default: invalidJSONReply(context,requestBody);
                }
                checkResponseAndSendReply(context, b);
            } else {
                invalidJSONReply(context,requestBody);
            }
        }).start();
    }

    private void checkServiceAndPlugIt(JsonObject requestBody) {
        String serviceName = requestBody.getString(SERVICE_NAME);
        switch (serviceName) {
            case "BikeService":
                if (!serviceLookup.isBikeServiceConnected()) {
                    serviceLookup.plugBikeService(requestBody.getString(SERVICE_ADDRESS),
                            requestBody.getInteger(SERVICE_PORT));
                }
                break;
            case "UserService":
                System.out.println("pipo");
                if (!serviceLookup.isUserServiceConnected()) {
                    serviceLookup.plugUserService(requestBody.getString(SERVICE_ADDRESS),
                            requestBody.getInteger(SERVICE_PORT));
                }
                break;
            case "RideService":
                if (!serviceLookup.isRideServiceConnected()) {
                    serviceLookup.plugRideService(requestBody.getString(SERVICE_ADDRESS),
                            requestBody.getInteger(SERVICE_PORT));
                }
                break;
        }
    }

    private void checkServiceAndHandleTopic(JsonObject requestBody) {
        String topic = requestBody.getString(TOPIC);
        switch (topic) {
            case BIKE_CHANGE_EVENT_TOPIC:
                break;
            case USER_CHANGE_EVENT_TOPIC:
                break;
            default:
        }
    }

    private void checkServiceAndUnplugIt(JsonObject requestBody) {
        String serviceName = requestBody.getString(SERVICE_NAME);
        switch (serviceName) {
            case "BikeService":
                if (serviceLookup.isBikeServiceConnected()) {
                    serviceLookup.unplugBikeService();
                }
                break;
            case "UserService":
                if (serviceLookup.isUserServiceConnected()) {
                    serviceLookup.unplugUserService();
                }
                break;
            case "RideService":
                if (serviceLookup.isRideServiceConnected()) {
                    serviceLookup.unplugRideService();
                }
                break;
        }
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

    private void invalidJSONReply(RoutingContext context, JsonObject requestBody) {
        LOGGER.warning("Received invalid JSON payload: " + requestBody);
        JsonObject reply = new JsonObject();
        reply.put(RESULT, "not ok");
        sendReply(context, reply);
    }

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
