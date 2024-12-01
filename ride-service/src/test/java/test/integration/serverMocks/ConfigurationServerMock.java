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


public class ConfigurationServerMock extends AbstractVerticle{

    private final int port;

    Vertx vertx;

    public ConfigurationServerMock() {
        this.port = 8099;
    }

    @Override
    public void start() {

        this.vertx = VertxSingleton.getInstance().getVertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.GET, "/api/configuration/query").handler(this::processServiceMockConfiguration);

        server.requestHandler(router).listen(port);
    }

    protected void processServiceMockConfiguration(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        JsonObject reply = new JsonObject();
        reply.put("BATTERY_CONSUMPTION_PER_METER",1);
        reply.put("CREDIT_CONSUMPTION_PER_SECOND",1);
        response.end(reply.toString());
    }

}
