package sap.ass02.bikeservice;
import io.vertx.core.AbstractVerticle;

public class Verticle2 extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().consumer("GRODUS", message -> {
            System.out.println("Received message: " + message.body());
        });
    }
}