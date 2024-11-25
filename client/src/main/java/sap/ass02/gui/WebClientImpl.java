package sap.ass02.gui;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.gui.GUI.EBikeApp;
import sap.ass02.gui.utils.Pair;
import sap.ass02.gui.utils.Triple;
import sap.ass02.gui.utils.WebOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.gui.utils.JsonFieldsConstants.*;

public class WebClientImpl implements WebClient {

    private static final Logger LOGGER = Logger.getLogger(WebClientImpl.class.getName());
    private static final String SERVER_HOST = "localhost";
    private static final int API_GATEWAY_SERVER_PORT = 8085;
    private static final String USER_COMMAND_PATH = "/api/user/command";
    private static final String USER_QUERY_PATH = "/api/user/query";
    private static final String EBIKE_COMMAND_PATH = "/api/ebike/command";
    private static final String EBIKE_QUERY_PATH = "/api/ebike/query";
    private static final String RIDE_COMMAND_PATH = "/api/ride/command";
    private static final String RIDE_QUERY_PATH = "/api/ride/query";
    private static final String BIKE_CHANGE_EVENT_TOPIC = "ebike-Change";
    private static final String USER_CHANGE_EVENT_TOPIC = "users-Change";

    private io.vertx.ext.web.client.WebClient apiClient;
    private Vertx vertx;

    public WebClientImpl() {
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");

        hazelcastConfig.getNetworkConfig().setPort(5701).getJoin().getTcpIpConfig().setEnabled(true).addMember("192.168.1.79:5701");

        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","Client");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));

        VertxOptions optionss = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(optionss, cluster -> {
            if (cluster.succeeded()) {
                vertx = cluster.result();
                WebClientOptions options = new WebClientOptions().setDefaultHost(SERVER_HOST).setDefaultPort(API_GATEWAY_SERVER_PORT);
                apiClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
                LOGGER.setLevel(Level.FINE);
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }

    @Override
    public Future<Boolean> requestCreateUser(String username, String password) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USERNAME, username);
        requestPayload.put(PASSWORD, password);
        requestPayload.put(OPERATION, WebOperation.CREATE.ordinal());

        apiClient.post(USER_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("User created: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to create user: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestDeleteUser(int userId) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(OPERATION, WebOperation.DELETE.ordinal());

        apiClient.post(USER_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("User deleted: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to delete user: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestUpdateUser(int userId, int credit) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(CREDIT, credit);
        requestPayload.put(OPERATION, WebOperation.UPDATE.ordinal());

        apiClient.post(USER_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("User updated: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to update user: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestUpdateUserRole(int userId, boolean admin) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(ADMIN, admin ? 1 : 0);
        requestPayload.put(OPERATION, WebOperation.UPDATE.ordinal());

        apiClient.post(USER_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("User updated: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to update user: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestLogin(String username, String password) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USERNAME, username);
        requestPayload.put(PASSWORD, password);
        requestPayload.put(OPERATION, WebOperation.LOGIN.ordinal());

        apiClient.get(USER_QUERY_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("Login: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.warning("Login failed: " + ar.result().bodyAsString());
                            }
                            promise.complete(false);
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to login: " + ar.cause().getMessage());
                        }
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Map<Integer,Triple<String,Integer,Boolean>>> requestReadUser(int userId, String username) {
        Promise<Map<Integer,Triple<String,Integer,Boolean>>> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        Map<Integer, Triple<String, Integer, Boolean>> retMap = new ConcurrentHashMap<>();
        if (userId > 0) {
            requestPayload.put(USER_ID, userId);
        }
        if (!username.isBlank()) {
            requestPayload.put(USERNAME, username);
        }
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        apiClient.get(USER_QUERY_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        JsonObject res = ar.result().bodyAsJsonObject();
                        if (res.containsKey(RESULT)) {
                            var resList = res.getJsonArray(RESULT);
                            var it = resList.stream().iterator();
                            while (it.hasNext()) {
                                var jsonObj = (JsonObject) it.next();
                                int resId = Integer.parseInt(jsonObj.getString(USER_ID));
                                var resUser = new Triple<>(jsonObj.getString(USERNAME), Integer.parseInt(jsonObj.getString(CREDIT)), Boolean.parseBoolean(jsonObj.getString("admin")));
                                retMap.put(resId, resUser);
                            }
                            promise.complete(retMap);
                        } else if (res.containsKey(USER_ID)) {
                            int resId = Integer.parseInt(res.getString(USER_ID));
                            var resUser = new Triple<>(res.getString(USERNAME), Integer.parseInt(res.getString(CREDIT)), Boolean.parseBoolean(res.getString("admin")));
                            retMap.put(resId, resUser);
                            promise.complete(retMap);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.warning("Error in response received from server");
                            }
                            promise.complete(null);
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to retrieve users: " + ar.cause().getMessage());
                        }
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestCreateEBike(int x, int y) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(POSITION_X, x);
        requestPayload.put(POSITION_Y, y);
        requestPayload.put(OPERATION, WebOperation.CREATE.ordinal());

        apiClient.post(EBIKE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("EBike created: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to create eBike: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestDeleteEBike(int eBikeId) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(E_BIKE_ID, eBikeId);
        requestPayload.put(OPERATION, WebOperation.DELETE.ordinal());

        apiClient.post(EBIKE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("eBike deleted: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to delete eBike: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestUpdateEBike(int eBikeId, int battery, String state, int x, int y) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(E_BIKE_ID, eBikeId);
        requestPayload.put(POSITION_X, x);
        requestPayload.put(POSITION_Y, y);
        if (battery > 0) {
            requestPayload.put(BATTERY, battery);
        }
        if (state != null) {
            requestPayload.put("state", state);
        }
        requestPayload.put(OPERATION, WebOperation.UPDATE.ordinal());

        apiClient.post(EBIKE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("eBike updated: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to update eBike: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>>> requestReadEBike(int eBikeId, int x, int y, boolean available) {
        Promise<Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>>> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>> retMap = new ConcurrentHashMap<>();
        if (eBikeId > 0) {
            requestPayload.put(E_BIKE_ID, eBikeId);
        } else {
            requestPayload.put("available", available);
        }
        if (x > 0) {
            requestPayload.put(POSITION_X, x);
        }
        if (y > 0) {
            requestPayload.put(POSITION_Y, y);
        }

        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        apiClient.get(EBIKE_QUERY_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.info("eBikes: " + ar.result().bodyAsString());
                        }
                        JsonObject res = ar.result().bodyAsJsonObject();
                        if (res.containsKey(RESULT)) {
                            var resList = res.getJsonArray(RESULT);
                            var it = resList.stream().iterator();
                            while (it.hasNext()) {
                                var jsonObj = (JsonObject) it.next();
                                insertEBikeInMap(retMap, jsonObj);
                            }
                            promise.complete(retMap);
                        } else if (res.containsKey(E_BIKE_ID)) {
                            insertEBikeInMap(retMap, res);
                            promise.complete(retMap);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.warning("Error in response received from server");
                            }
                            promise.complete(null);
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to retrieve eBikes: " + ar.cause().getMessage());
                        }
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestStartRide(int userId, int eBikeId) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(E_BIKE_ID, eBikeId);
        requestPayload.put(OPERATION, WebOperation.CREATE.ordinal());

        apiClient.post(RIDE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded() && ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.info("ride started: " + ar.result().bodyAsString());
                        }
                        promise.complete(true);
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to start ride: " + ar.cause().getMessage());
                        }
                        promise.complete(false);
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Pair<Integer,Integer>> requestUpdateRide(int userId, int eBikeId, int x, int y) {
        Promise<Pair<Integer,Integer>> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(E_BIKE_ID, eBikeId);
        requestPayload.put(POSITION_X, x);
        requestPayload.put(POSITION_Y, y);
        requestPayload.put(OPERATION, WebOperation.UPDATE.ordinal());

        apiClient.post(RIDE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        var res = ar.result().bodyAsJsonObject();
                        if (res.containsKey(CREDIT) && res.containsKey(BATTERY)) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("ride updated: " + ar.result().bodyAsString());
                            }
                            promise.complete(new Pair<>(Integer.parseInt(res.getString(CREDIT)),
                                    Integer.parseInt(res.getString(BATTERY))));
                        } else {
                            promise.complete(null);
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to update ride: " + ar.cause().getMessage());
                        }
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestEndRide(int userId, int eBikeId) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USER_ID, userId);
        requestPayload.put(E_BIKE_ID, eBikeId);
        requestPayload.put(OPERATION, WebOperation.DELETE.ordinal());

        apiClient.post(RIDE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded() && ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.info("ride ended: " + ar.result().bodyAsString());
                        }
                        promise.complete(true);
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to end ride: " + ar.cause().getMessage());
                        }
                        promise.complete(false);
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestDeleteRide(int rideId) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put("rideId", rideId);
        requestPayload.put(OPERATION, WebOperation.DELETE.ordinal());

        apiClient.post(RIDE_COMMAND_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.info("ride deleted: " + ar.result().bodyAsString());
                            }
                            promise.complete(true);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.severe("Failed to delete ride: " + ar.cause().getMessage());
                            }
                            promise.complete(false);
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Map<Integer,Pair<Pair<Integer, Integer>,Pair<String, String>>>> requestMultipleReadRide(int userId, int eBikeId, boolean ongoing) {
        Promise<Map<Integer,Pair<Pair<Integer, Integer>,Pair<String, String>>>> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        Map<Integer,Pair<Pair<Integer, Integer>,Pair<String, String>>> retMap = new ConcurrentHashMap<>();
        if (eBikeId > 0) {
            requestPayload.put(E_BIKE_ID, eBikeId);
        }
        if (userId > 0) {
            requestPayload.put(USER_ID, userId);
        }
        if (ongoing) {
            requestPayload.put("ongoing", true);
        }
        requestPayload.put("multiple", true);
        requestPayload.put(OPERATION, WebOperation.READ.ordinal());

        apiClient.get(RIDE_QUERY_PATH)
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.info("rides: " + ar.result().bodyAsString());
                        }
                        var res = ar.result().bodyAsJsonObject();
                        if (res.containsKey(RESULT)) {
                            var resList = res.getJsonArray(RESULT);
                            var it = resList.stream().iterator();
                            while (it.hasNext()) {
                                var jsonObj = (JsonObject) it.next();
                                int resId = Integer.parseInt(jsonObj.getString("rideId"));
                                var resUser = new Pair<>(new Pair<>(Integer.parseInt(jsonObj.getString(USER_ID)),Integer.parseInt(jsonObj.getString(E_BIKE_ID))),
                                        new Pair<>(jsonObj.getString("startDate"), jsonObj.getString("endDate")));
                                retMap.put(resId, resUser);
                            }
                            promise.complete(retMap);
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.severe("Failed to retrieve rides: " + ar.cause().getMessage());
                        }
                    }
                });
        return promise.future();
    }

    @Override
    public void startMonitoringEBike(EBikeApp app) {
        vertx.eventBus().consumer(BIKE_CHANGE_EVENT_TOPIC, msg -> {
            JsonObject jsonMessage = (JsonObject) msg.body();
            if (jsonMessage.containsKey(E_BIKE_ID) && jsonMessage.containsKey(POSITION_X) &&
                    jsonMessage.containsKey(POSITION_Y) && jsonMessage.containsKey(BATTERY) && jsonMessage.containsKey("status")){
                int eBikeId = Integer.parseInt(jsonMessage.getString(E_BIKE_ID));
                int x = Integer.parseInt(jsonMessage.getString(POSITION_X));
                int y = Integer.parseInt(jsonMessage.getString(POSITION_Y));
                int battery = Integer.parseInt(jsonMessage.getString(BATTERY));
                String status = jsonMessage.getString("status");
                app.updateEBikeFromEventbus(eBikeId, x, y, battery, status);
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.info("Received message from eventBus: " + jsonMessage);
            }
        });
    }

    @Override
    public void startMonitoringUsers(EBikeApp app) {
        vertx.eventBus().consumer(USER_CHANGE_EVENT_TOPIC, msg -> {
            JsonObject jsonMessage = (JsonObject) msg.body();
            if (jsonMessage.containsKey(USER_ID) && jsonMessage.containsKey(CREDIT)){
                int userId = Integer.parseInt(jsonMessage.getString(USER_ID));
                int credit = Integer.parseInt(jsonMessage.getString(CREDIT));
                if (app.getUserId() == userId) {
                    app.updateUserFromEventbus(credit);
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.info("Received message from eventBus: " + jsonMessage);
            }
        });
    }

    private void insertEBikeInMap(Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>> retMap, JsonObject jsonObj) {
        int resId = Integer.parseInt(jsonObj.getString(E_BIKE_ID));
        var resBike = new Triple<>(new Pair<>(Integer.parseInt(jsonObj.getString(POSITION_X)), Integer.parseInt(jsonObj.getString(POSITION_Y))),
                Integer.parseInt(jsonObj.getString(BATTERY)), jsonObj.getString("status"));
        retMap.put(resId, resBike);
    }

}
