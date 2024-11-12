package sap.ass02.apigateway;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiGateway extends AbstractVerticle {

    private static final String USER_COMMAND_PATH = "/api/user/command";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String EBIKE_COMMAND_PATH = "/api/ebike/command";
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String RIDE_COMMAND_PATH = "/api/ride/command";
    private static final String RIDE_QUERY_PATH = "/api/ride/query";

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
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8085");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new ClusterMembershipListenerImpl(this.serviceLookup)));
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

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

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena Api Gateway ready on port: " + port);
        }

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

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
