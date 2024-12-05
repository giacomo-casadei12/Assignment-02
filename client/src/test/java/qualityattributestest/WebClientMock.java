package qualityattributestest;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import sap.ass02.gui.GUI.EBikeApp;
import sap.ass02.gui.WebClient;
import sap.ass02.gui.utils.Pair;
import sap.ass02.gui.utils.Triple;
import sap.ass02.gui.utils.WebOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static sap.ass02.gui.utils.JsonFieldsConstants.*;
import static sap.ass02.gui.utils.JsonFieldsConstants.RESULT;

public class WebClientMock implements WebClient {

    final Vertx vertx;
    final io.vertx.ext.web.client.WebClient apiClient;

    public WebClientMock() {
        vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8085);
        apiClient = io.vertx.ext.web.client.WebClient.create(vertx, options);
    }

    @Override
    public Future<Boolean> requestCreateUser(String username, String password) {
        return null;
    }

    @Override
    public Future<Boolean> requestDeleteUser(int userId) {
        return null;
    }

    @Override
    public Future<Boolean> requestUpdateUser(int userId, int credit) {
        return null;
    }

    @Override
    public Future<Boolean> requestUpdateUserRole(int userId, boolean admin) {
        return null;
    }

    @Override
    public Future<Boolean> requestLogin(String username, String password) {
        Promise<Boolean> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
        requestPayload.put(USERNAME, username);
        requestPayload.put(PASSWORD, password);
        requestPayload.put(OPERATION, WebOperation.LOGIN.ordinal());

        apiClient.get("/api/user/query")
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            promise.complete(true);
                        } else {
                            promise.complete(false);
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Map<Integer, Triple<String, Integer, Boolean>>> requestReadUser(int userId, String username) {
        return null;
    }

    @Override
    public Future<Boolean> requestCreateEBike(int x, int y) {
        return null;
    }

    @Override
    public Future<Boolean> requestDeleteEBike(int eBikeId) {
        return null;
    }

    @Override
    public Future<Boolean> requestUpdateEBike(int eBikeId, int battery, String state, int x, int y) {
        return null;
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

        apiClient.get("/api/ebike/query")
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        JsonObject res = ar.result().bodyAsJsonObject();
                        if (res.containsKey(RESULT)) {
                            res.getJsonArray(RESULT);
                            promise.complete(retMap);
                        } else if (res.containsKey(E_BIKE_ID)) {
                            promise.complete(retMap);
                        } else {
                            promise.complete(null);
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Boolean> requestStartRide(int userId, int eBikeId) {
        return null;
    }

    @Override
    public Future<Pair<Integer, Integer>> requestUpdateRide(int userId, int eBikeId, int x, int y) {
        return null;
    }

    @Override
    public Future<Boolean> requestEndRide(int userId, int eBikeId) {
        return null;
    }

    @Override
    public Future<Boolean> requestDeleteRide(int rideId) {
        return null;
    }

    @Override
    public Future<Map<Integer, Pair<Pair<Integer, Integer>, Pair<String, String>>>> requestMultipleReadRide(int userId, int eBikeId, boolean ongoing) {
        Promise<Map<Integer, Pair<Pair<Integer, Integer>, Pair<String, String>>>> promise = Promise.promise();
        JsonObject requestPayload = new JsonObject();
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
        Map<Integer, Pair<Pair<Integer, Integer>, Pair<String, String>>> res = new HashMap<>();

        apiClient.get("/api/ride/query")
                .sendJson(requestPayload, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().bodyAsJsonObject().getValue(RESULT).toString().equals("ok")) {
                            promise.complete(res);
                        } else {
                            promise.complete(res);
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public void startMonitoringEBike(EBikeApp app) {

    }

    @Override
    public void startMonitoringUsers(EBikeApp app) {

    }
}
