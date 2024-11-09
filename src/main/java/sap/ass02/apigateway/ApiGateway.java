package sap.ass02.apigateway;

import com.hazelcast.config.Config;
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
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiGateway extends AbstractVerticle {

    private static final String SERVER_HOST = "localhost";
    private static final int RIDE_SERVER_PORT = 8080;
    private static final int USER_SERVER_PORT = 8081;
    private static final int BIKE_SERVER_PORT = 8082;

    private static final String USER_COMMAND_PATH = "/api/user/command";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String EBIKE_COMMAND_PATH = "/api/ebike/command";
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String RIDE_COMMAND_PATH = "/api/ride/command";
    private static final String RIDE_QUERY_PATH = "/api/ride/query";

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private Vertx vertx;
    private WebClient rideClient, userClient, bikeClient;

    public ApiGateway() {
        this.port = 8085;
        LOGGER.setLevel(Level.FINE);
        Config hazelcastConfig = new Config();

        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions vOptions = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(vOptions, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                WebClientOptions options = new WebClientOptions().setDefaultHost(SERVER_HOST).setDefaultPort(USER_SERVER_PORT);
                userClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                options = new WebClientOptions().setDefaultHost(SERVER_HOST).setDefaultPort(BIKE_SERVER_PORT);
                bikeClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                options = new WebClientOptions().setDefaultHost(SERVER_HOST).setDefaultPort(RIDE_SERVER_PORT);
                rideClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
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
        bikeClient.post(EBIKE_COMMAND_PATH)
                  .sendJson(context.body().asJsonObject(), ar -> {
                      if (ar.succeeded()) {
                          sendReply(context, ar.result().bodyAsJsonObject());
                      } else {
                          LOGGER.severe(ar.cause().getMessage());
                      }
                  });
    }

    protected void processServiceEBikeQuery(RoutingContext context) {
        bikeClient.get(EBIKE_QUERY_PATH)
                  .sendJson(context.body().asJsonObject(), ar -> {
                      if (ar.succeeded()) {
                          sendReply(context, ar.result().bodyAsJsonObject());
                      } else {
                          LOGGER.severe(ar.cause().getMessage());
                      }
                  });
    }

    protected void processServiceRideCmd(RoutingContext context) {
        rideClient.post(RIDE_COMMAND_PATH)
                .sendJson(context.body().asJsonObject(), ar -> {
                    if (ar.succeeded()) {
                        sendReply(context, ar.result().bodyAsJsonObject());
                    } else {
                        LOGGER.severe(ar.cause().getMessage());
                    }
                });
    }

    protected void processServiceRideQuery(RoutingContext context) {
        rideClient.get(RIDE_QUERY_PATH)
                .sendJson(context.body().asJsonObject(), ar -> {
                    if (ar.succeeded()) {
                        sendReply(context, ar.result().bodyAsJsonObject());
                    } else {
                        LOGGER.severe(ar.cause().getMessage());
                    }
                });
    }

    protected void processServiceUserCmd(RoutingContext context) {
        userClient.post(USER_COMMAND_PATH)
                .sendJson(context.body().asJsonObject(), ar -> {
                    if (ar.succeeded()) {
                        sendReply(context, ar.result().bodyAsJsonObject());
                    } else {
                        LOGGER.severe(ar.cause().getMessage());
                    }
                });
    }

    protected void processServiceUserQuery(RoutingContext context) {
        userClient.get(USER_QUERY_PATH)
                .sendJson(context.body().asJsonObject(), ar -> {
                    if (ar.succeeded()) {
                        sendReply(context, ar.result().bodyAsJsonObject());
                    } else {
                        LOGGER.severe(ar.cause().getMessage());
                    }
                });
    }

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
