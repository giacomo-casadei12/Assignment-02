package test.integration;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sap.ass02.vertxrideservice.domain.ports.AppManager;
import sap.ass02.vertxrideservice.infrastructure.ServiceLookup;
import sap.ass02.vertxrideservice.infrastructure.ServiceLookupImpl;
import sap.ass02.vertxrideservice.infrastructure.WebController;
import sap.ass02.vertxrideservice.utils.JsonFieldsConstants;
import sap.ass02.vertxrideservice.utils.VertxSingleton;
import sap.ass02.vertxrideservice.utils.WebOperation;
import test.integration.serverMocks.ApiGatewayMock;
import test.integration.serverMocks.ConfigurationServerMock;

import java.util.concurrent.TimeUnit;

import static sap.ass02.vertxrideservice.utils.JsonFieldsConstants.*;

@ExtendWith(VertxExtension.class)
class TestControllerLogicIntegration {

    private static VertxSingleton vertxSingleton;
    private JsonObject json;

    @BeforeEach
    public void setUpBeforeClass() {
        Vertx vertx = Vertx.vertx();
        vertxSingleton = new VertxSingleton();
        vertxSingleton.setVertx(vertx);
    }

    @Test
    public void testGetRideByUserIdControllerLogic(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        vertxSingleton.getVertx().deployVerticle(new AppManager());
        vertxSingleton.getVertx().deployVerticle(new WebController(new ServiceLookupImpl()));

        Thread.sleep(2000);

        json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json.put(JsonFieldsConstants.USER_ID, 1);

        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
        WebClient client = WebClient.create(vertx, options);
        client.get("/api/ride/query").sendJson(json);

        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 1);
        busPayload.put(USER_ID, 1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RidePersistenceGetRideByUser");
        consumer.handler(message -> {
            if (busPayload.toString().equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }

    @Test
    public void testGetRideByRideIdControllerLogic(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        vertxSingleton.getVertx().deployVerticle(new AppManager());
        vertxSingleton.getVertx().deployVerticle(new WebController(new ServiceLookupImpl()));

        Thread.sleep(2000);

        json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json.put(RIDE_ID, 1);

        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
        WebClient client = WebClient.create(vertx, options);
        client.get("/api/ride/query").sendJson(json);

        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 1);
        busPayload.put(RIDE_ID, 1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RidePersistenceGetRideByRideId");
        consumer.handler(message -> {
            if (busPayload.toString().equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }

    @Test
    public void testGetAllRidesControllerLogic(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        vertxSingleton.getVertx().deployVerticle(new AppManager());
        vertxSingleton.getVertx().deployVerticle(new WebController(new ServiceLookupImpl()));

        Thread.sleep(2000);

        json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json.put("multiple", true);

        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
        WebClient client = WebClient.create(vertx, options);
        client.get("/api/ride/query").sendJson(json);

        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RidePersistenceGetAllRides");
        consumer.handler(message -> {
            if (busPayload.toString().equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }

    @Test
    public void testDeleteRideControllerLogic(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        vertxSingleton.getVertx().deployVerticle(new AppManager());
        vertxSingleton.getVertx().deployVerticle(new WebController(new ServiceLookupImpl()));

        Thread.sleep(2000);

        json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.DELETE.ordinal());
        json.put(RIDE_ID, 1);

        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
        WebClient client = WebClient.create(vertx, options);
        client.post("/api/ride/command").sendJson(json);

        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 1);
        busPayload.put(RIDE_ID, 1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RidePersistenceDeleteRide");
        consumer.handler(message -> {
            if (busPayload.toString().equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }

    @Test
    public void testIntegrationWithApiGateway(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        ServiceLookup sl = new ServiceLookupImpl();
        sl.setVertxInstance(vertxSingleton.getVertx());
        sl.plugAPIGatewayService("localhost",8095);
        vertxSingleton.getVertx().deployVerticle(new ApiGatewayMock());
        vertxSingleton.getVertx().deployVerticle(new AppManager());
        vertxSingleton.getVertx().deployVerticle(new WebController(sl));

        Thread.sleep(2000);

        json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.CREATE.ordinal());
        json.put(USER_ID, 1);
        json.put(E_BIKE_ID, 1);

        WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
        WebClient client = WebClient.create(vertx, options);
        client.post("/api/ride/command").sendJson(json);

        JsonObject busPayload = new JsonObject();
        busPayload.put("RequestId", 1);
        busPayload.put(USER_ID, 1);
        busPayload.put(E_BIKE_ID, 1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RidePersistenceCreateRide");
        consumer.handler(message -> {
            if (busPayload.toString().equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }

    @Test
    public void testIntegrationWithConfigurationServer(Vertx vertx, VertxTestContext vertxTestContext) throws InterruptedException {

        ServiceLookup sl = new ServiceLookupImpl();
        sl.setVertxInstance(vertxSingleton.getVertx());
        sl.plugConfigurationServer("localhost",8099);
        vertxSingleton.getVertx().deployVerticle(new ConfigurationServerMock());
        vertxSingleton.getVertx().deployVerticle(new WebController(sl));

        JsonObject busPayload = new JsonObject();
        busPayload.put("BATTERY_CONSUMPTION_PER_METER",1);
        busPayload.put("CREDIT_CONSUMPTION_PER_SECOND",1);
        MessageConsumer<String> consumer = VertxSingleton.getInstance().getVertx().eventBus().consumer("RideServiceUpdateConfigurations");
        consumer.handler(message -> {
            //non so perchè qui il messaggio viene considerato un JsonObject
            if (busPayload.equals(message.body())) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow("Messages doesn't match");
            }
        });

        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
        VertxSingleton.getInstance().getVertx().close();
    }
}
