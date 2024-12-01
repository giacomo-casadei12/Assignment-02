package test.integration.serverMocks;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import sap.ass02.vertxrideservice.utils.VertxSingleton;

import static sap.ass02.vertxrideservice.utils.JsonFieldsConstants.*;

public class ApiGatewayMock extends AbstractVerticle {

    private final int port;

    Vertx vertx;

    public ApiGatewayMock() {
        this.port = 8095;
    }

    @Override
    public void start() {

        this.vertx = VertxSingleton.getInstance().getVertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.GET, "/api/user/query").handler(this::processServiceMockUser);
        router.route(HttpMethod.GET, "/api/ebike/query").handler(this::processServiceMockBike);

        server.requestHandler(router).listen(port);
    }

    protected void processServiceMockUser(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        JsonObject reply = new JsonObject();
        reply.put(USER_ID,1);
        reply.put(CREDIT,100);
        reply.put(ADMIN,"true");
        reply.put(USERNAME,"test");
        response.end(reply.toString());
    }

    protected void processServiceMockBike(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        JsonObject reply = new JsonObject();
        reply.put(E_BIKE_ID,1);
        reply.put(BATTERY,100);
        reply.put(POSITION_X,4);
        reply.put(POSITION_Y,5);
        reply.put("status","AVAILABLE");
        response.end(reply.toString());
    }

}
