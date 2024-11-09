package sap.ass02.userservice;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class Verticle1 extends AbstractVerticle {

    @Override
    public void start() {
        /*vertx.setPeriodic(2000, id -> {
            vertx.eventBus().send("GRODUS", "Hello from Producer!");
            System.out.println("Message sent to event bus.");
        });*/
        // Set up HTTP server
        Router router = Router.router(vertx);

        // Define an HTTP route
        router.get("/").handler(ctx -> {
            // Use Hazelcast here if needed
            ctx.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello, Vert.x HTTP server running inside a Hazelcast cluster!");
        });

        // Create and start the HTTP server
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router).listen(8080, res -> {
            if (res.succeeded()) {
                System.out.println("HTTP server started on port 8080");
            } else {
                System.out.println("Failed to start HTTP server: " + res.cause());
            }
        });
    }
}