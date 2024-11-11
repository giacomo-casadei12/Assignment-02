package sap.ass02.userservice.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
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
import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.ports.AppManager;
import sap.ass02.userservice.utils.WebOperation;

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
    private static final String USER_CHANGE_EVENT_TOPIC = "users-Change";
    /**
     * The Vertx.
     */
    Vertx vertx;
    final private AppManager pManager;

    /**
     * Instantiates a new Web controller.
     */
    public WebController(AppManager appManager) {
        this.port = 8081;
        LOGGER.setLevel(Level.FINE);
        this.pManager = appManager;
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","UserService");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8081");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
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

        router.route(HttpMethod.POST, "/api/user/command").handler(this::processServiceUserCmd);
        router.route(HttpMethod.GET, "/api/user/query").handler(this::processServiceUserQuery);

        server.requestHandler(router).listen(port);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "EBikeCesena web server ready on port: " + port);
        }

    }

    /**
     * Process a request that will need to WRITE
     * the user persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceUserCmd(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - user cmd " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                boolean b = false;
                switch (op) {
                    case CREATE:  {
                        if (requestBody.containsKey(USERNAME) && requestBody.containsKey(PASSWORD)) {
                            String username = requestBody.getString(USERNAME);
                            String password = requestBody.getString(PASSWORD);
                            b = pManager.createUser(username, password);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case UPDATE:  {
                        if (requestBody.containsKey(USER_ID) && requestBody.containsKey(CREDIT)) {
                            int id = requestBody.getInteger(USER_ID);
                            int credit = requestBody.getInteger(CREDIT);
                            b = pManager.updateUser(id,credit);
                        } else {
                            invalidJSONReply(context,requestBody);
                        }
                        break;
                    }
                    case DELETE:  {
                        if (requestBody.containsKey(USER_ID)) {
                            int id = requestBody.getInteger(USER_ID);
                            b = pManager.deleteUser(id);
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

    /**
     * Process a request that will need to READ
     * the user persistence system.
     *
     * @param context the RoutingContext
     */
    protected void processServiceUserQuery(RoutingContext context) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.INFO, "New request - user query " + context.currentRoute().getPath());
        }
        new Thread(() -> {
            // Parse the JSON body
            JsonObject requestBody = context.body().asJsonObject();
            if (requestBody != null && requestBody.containsKey(OPERATION)) {
                WebOperation op = WebOperation.values()[requestBody.getInteger(OPERATION)];
                boolean b;
                User u;
                List<User> users;
                if (op == WebOperation.LOGIN) {
                    if (requestBody.containsKey(USERNAME) && requestBody.containsKey(PASSWORD)) {
                        String username = requestBody.getString(USERNAME);
                        String password = requestBody.getString(PASSWORD);
                        b = pManager.login(username, password);
                        checkResponseAndSendReply(context, b);
                    } else {
                        invalidJSONReply(context,requestBody);
                    }
                } else if(op == WebOperation.READ) {
                        if (requestBody.containsKey(USER_ID) || requestBody.containsKey(USERNAME)) {
                            int id = requestBody.containsKey(USER_ID) ? requestBody.getInteger(USER_ID) : 0;
                            String username = requestBody.containsKey(USERNAME) ? requestBody.getString(USERNAME) : "";
                            u = pManager.getUser(id,username);
                            var map = new HashMap<String, Object>();
                            map.put(USER_ID, u.id());
                            map.put(USERNAME, u.userName());
                            map.put(CREDIT, u.credit());
                            map.put("admin", u.admin());
                            composeJSONAndSendReply(context,map);
                        } else {
                            users = pManager.getAllUsers();
                            var array = new ArrayList<Map<String,Object>>();
                            for (User user : users) {
                                var map = new HashMap<String, Object>();
                                map.put(USER_ID, user.id());
                                map.put(USERNAME, user.userName());
                                map.put(CREDIT, user.credit());
                                map.put("admin", user.admin());
                                array.add(map);
                            }
                            composeJSONArrayAndSendReply(context,array);
                        }
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

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

}
