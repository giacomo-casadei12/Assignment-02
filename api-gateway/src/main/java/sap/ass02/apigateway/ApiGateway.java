package sap.ass02.apigateway;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
    private static final String HEALTH_CHECK_PATH = "/healthCheck";

    private static final String BIKE_CHANGE_EVENT_TOPIC = "ebike-Change";
    private static final String USER_CHANGE_EVENT_TOPIC = "users-Change";

    private final ServiceLookup serviceLookup;

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private Vertx vertx;

    public ApiGateway() {
        this.port = 8085;
        LOGGER.setLevel(Level.FINE);
        this.serviceLookup = new ServiceLookupImpl();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","ApiGateway");
        attributes.put("SERVICE_ADDRESS","api-gateway");
        attributes.put("SERVICE_PORT","8085");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new ClusterMembershipListenerImpl(this.serviceLookup)));

        hazelcastConfig.getNetworkConfig().setPort(5701).getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("192.168.1.79:5701")
                .addMember("api-gateway:5701");

        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions vOptions = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(vOptions, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
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

        router.route(HttpMethod.GET, HEALTH_CHECK_PATH).handler(this::healthCheckHandler);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena Api Gateway ready on port: " + port);
        }

        vertx.eventBus().consumer("UserChangedFromUserService", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.INFO, "notify user changed");
            }
            EventBus eb = vertx.eventBus();

            JsonObject obj = new JsonObject();
            obj.put("event", USER_CHANGE_EVENT_TOPIC);
            obj.put(USER_ID, json.getInteger(USER_ID));
            obj.put(CREDIT, json.getInteger(CREDIT));
            eb.publish(USER_CHANGE_EVENT_TOPIC, obj);
        });

        vertx.eventBus().consumer("BikeChangedFromBikeService", msg -> {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.INFO, "notify ebike changed");
            }
            EventBus eb = vertx.eventBus();
            JsonObject obj = new JsonObject(msg.body().toString());
            obj.put("event", BIKE_CHANGE_EVENT_TOPIC);

            eb.publish(BIKE_CHANGE_EVENT_TOPIC, obj);

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

    protected void healthCheckHandler(RoutingContext context) {
        LOGGER.log(Level.INFO, "Health check request " + context.currentRoute().getPath());
        JsonObject reply = new JsonObject();
        reply.put("status", "UP");
        JsonArray checks = new JsonArray();
        reply.put("checks", checks);
        sendReply(context, reply);
    }

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
