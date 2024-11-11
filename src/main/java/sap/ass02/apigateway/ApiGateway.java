package sap.ass02.apigateway;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
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
import io.vertx.ext.web.client.WebClientOptions;
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

    private String ride_server_host = "";
    private String user_server_host = "";
    private String bike_server_host = "";
    private int ride_server_port = 0;
    private int user_server_port = 0;
    private int bike_server_port = 0;

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private Vertx vertx;
    private WebClient rideClient, userClient, bikeClient;

    public ApiGateway() {
        this.port = 8085;
        LOGGER.setLevel(Level.FINE);
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","ApiGateway");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8085");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new MembershipListener(){
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                Set<Member> members = membershipEvent.getMembers();
                for (Member member : members) {
                    String serviceName = member.getAttribute("SERVICE_NAME");
                    switch (serviceName) {
                        case "BikeService":
                            if (bike_server_host.isBlank() && bike_server_port == 0) {
                                bike_server_port = Integer.parseInt(member.getAttribute("SERVICE_PORT"));
                                bike_server_host = member.getAttribute("SERVICE_ADDRESS");
                                if (Objects.isNull(bikeClient) && !Objects.isNull(vertx)) {
                                    WebClientOptions options = new WebClientOptions().setDefaultHost(bike_server_host).setDefaultPort(bike_server_port);
                                    bikeClient = WebClient.create(vertx, options);
                                }
                            }
                            break;
                        case "UserService":
                            System.out.println("pipo");
                            if (user_server_host.isBlank() && user_server_port == 0) {
                                user_server_port = Integer.parseInt(member.getAttribute("SERVICE_PORT"));
                                user_server_host = member.getAttribute("SERVICE_ADDRESS");
                                if (Objects.isNull(userClient) && !Objects.isNull(vertx)) {
                                    WebClientOptions options = new WebClientOptions().setDefaultHost(user_server_host).setDefaultPort(user_server_port);
                                    userClient = WebClient.create(vertx, options);
                                }
                            }
                            break;
                        case "RideService":
                            if (ride_server_host.isBlank() && ride_server_port == 0) {
                                ride_server_port = Integer.parseInt(member.getAttribute("SERVICE_PORT"));
                                ride_server_host = member.getAttribute("SERVICE_ADDRESS");
                                if (Objects.isNull(rideClient) && !Objects.isNull(vertx)) {
                                    WebClientOptions options = new WebClientOptions().setDefaultHost(ride_server_host).setDefaultPort(ride_server_port);
                                    rideClient = WebClient.create(vertx, options);
                                }
                            }
                            break;
                    }
                }
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                System.out.println("non Groda");
            }
        }));
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions vOptions = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(vOptions, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                WebClientOptions options;
                if (!user_server_host.isBlank() && user_server_port > 0 && Objects.isNull(userClient)) {
                    options = new WebClientOptions().setDefaultHost(user_server_host).setDefaultPort(user_server_port);
                    userClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                }
                if (!bike_server_host.isBlank() && bike_server_port > 0 && Objects.isNull(bikeClient)) {
                    options = new WebClientOptions().setDefaultHost(bike_server_host).setDefaultPort(bike_server_port);
                    bikeClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                }
                if (!ride_server_host.isBlank() && ride_server_port > 0 && Objects.isNull(rideClient)) {
                    options = new WebClientOptions().setDefaultHost(ride_server_host).setDefaultPort(ride_server_port);
                    rideClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                }
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
