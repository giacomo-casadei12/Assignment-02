package sap.ass02.configurationserver.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.configurationserver.utils.JsonFieldsConstants.RESULT;


/**
 * The Vertx Server that handles all request
 * coming from clients.
 */
public class WebController extends AbstractVerticle implements ConfigurationSharer {

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private static final String HEALTH_CHECK_PATH = "/healthCheck";

    private final ClusterManager clusterManager;
    private IMap<String, String> distributedMap;
    private final Map<String, String> configurationMap = new ConcurrentHashMap<>();

    private final Gauge configurations_registered_gauge;
    private final Counter configurations_requested_counter;

    /**
     * The Vertx.
     */
    Vertx vertx;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(ConfigurationFilesObserver cfo ) {
        this.port = 8090;

        configurations_registered_gauge = Gauge.builder()
                .name("configurations_registered_counter")
                .help("number of configurations registered")
                .register();

        configurations_requested_counter = Counter.builder()
                .name("configurations_requested_counter")
                .help("number of configuration request received")
                .register();

        try {
            HTTPServer.builder()
                    .port(this.port+100)
                    .buildAndStart();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.setLevel(Level.FINE);
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","ConfigurationServer");
        attributes.put("SERVICE_ADDRESS","configuration-server");
        attributes.put("SERVICE_PORT","8090");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.getNetworkConfig().setPort(5701).getJoin().getTcpIpConfig().setEnabled(true).addMember("api-gateway:5701");
        clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                HazelcastInstance hz = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();
                distributedMap = hz.getMap("configurations");
                cfo.start(this);
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

        router.route(HttpMethod.GET, "/api/configuration/query").handler(this::processServiceConfigurationQuery);

        router.route(HttpMethod.GET, HEALTH_CHECK_PATH).handler(this::healthCheckHandler);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena web server ready on port: " + port);
        }

    }

    /**
     * Process a request that will need to READ
     * from the configuration server.
     *
     * @param context the RoutingContext
     */
    protected void processServiceConfigurationQuery(RoutingContext context) {
        configurations_requested_counter.inc();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - configuration get " + context.currentRoute().getPath());
        }
        JsonObject requestBody = context.body().asJsonObject();
        if (requestBody.containsKey("requestedConfig")) {
            String reqConfig = requestBody.getString("requestedConfig");
            JsonObject reply = new JsonObject();
            if (configurationMap.containsKey(reqConfig)) {
                reply = new JsonObject(configurationMap.get(reqConfig));
            } else {
                reply.put("Error", "Configuration not found");
            }
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.end(reply.toString());
        } else {
            invalidJSONReply(context,requestBody);
        }
    }

    protected void healthCheckHandler(RoutingContext context) {
        LOGGER.log(Level.INFO, "Health check request " + context.currentRoute().getPath());
        JsonObject reply = new JsonObject();
        reply.put("status", "UP");
        JsonArray checks = new JsonArray();
        //check if the configurations are present
        checks.add(!configurationMap.isEmpty());
        reply.put("checks", checks);
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    @Override
    public void addConfiguration(String configurationName, String configurationFile) {
        String confName = configurationName.split("\\.")[0];
        if(!configurationMap.containsKey(confName)) {
            configurations_registered_gauge.inc();
        }
        distributedMap.put(configurationName, new Date().toString());
        configurationMap.put(confName, configurationFile);
        System.out.println("Added configuration: " + configurationName);
    }

    @Override
    public void updateConfiguration(String configurationName, String configurationFile) {
        distributedMap.put(configurationName, new Date().toString());
        configurationMap.put(configurationName.split("\\.")[0], configurationFile);
        System.out.println("Changed configuration: " + configurationName);
    }

    private void invalidJSONReply(RoutingContext context, JsonObject requestBody) {
        LOGGER.warning("Received invalid JSON payload: " + requestBody);
        JsonObject reply = new JsonObject();
        reply.put(RESULT, "not ok");
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }
}
