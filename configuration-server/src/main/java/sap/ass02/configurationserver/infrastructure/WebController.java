package sap.ass02.configurationserver.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The Vertx Server that handles all request
 * coming from clients.
 */
public class WebController extends AbstractVerticle implements ConfigurationSharer {

    private final int port;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");
    private final ClusterManager clusterManager;
    private IMap<String, String> map;
    /**
     * The Vertx.
     */
    Vertx vertx;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(ConfigurationFilesObserver cfo ) {
        this.port = 8090;
        LOGGER.setLevel(Level.FINE);
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","ConfigurationServer");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8090");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                HazelcastInstance hz = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();

                map = hz.getMap("configurations");
                map.addEntryListener(new ClusterEntryListener(), true);
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

        router.route(HttpMethod.POST, "/api/user/command").handler(this::processServiceUserCmd);
        router.route(HttpMethod.GET, "/api/user/query").handler(this::processServiceUserQuery);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena web server ready on port: " + port);
        }

    }

    /*
    // Add an entry to the map (this will trigger the EntryListener)
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("name", "John Doe");
        jsonObject.put("age", 30);

        map.put("user", jsonObject.toString());

        // Retrieve the entry from the map
        String retrieved = map.get("user");
        System.out.println("Retrieved data: " + retrieved);
     */

    /**
     * Process a request that will need to WRITE
     * the user persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceUserCmd(RoutingContext context) {

    }

    /**
     * Process a request that will need to READ
     * the user persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceUserQuery(RoutingContext context) {

    }

    @Override
    public void addConfiguration(String configurationName, String configurationFile) {
        map.put(configurationName, configurationFile);
        System.out.println("Added configuration: " + configurationName);
        System.out.println("Added configuration file: " + new JsonObject(map.get(configurationName)).encodePrettily());
    }

    @Override
    public void updateConfiguration(String configurationName, String configurationFile) {
        map.put(configurationName, configurationFile);
        System.out.println("Changed configuration: " + configurationName);
        System.out.println("Changed configuration file: " + new JsonObject(map.get(configurationName)).encodePrettily());
    }
}
