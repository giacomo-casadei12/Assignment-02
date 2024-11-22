package sap.ass02.vertxrideservice.utils;

import io.vertx.core.Vertx;

/**
 * Represents the use of the singleton pattern for sharing the vertx instance
 */
final public class VertxSingleton {

    private static VertxSingleton instance;
    private Vertx vertx;

    public VertxSingleton() {
        instance = this;
    }

    /**
     * @return returns the vertx instance, if not present it creates it
     */
    public static VertxSingleton getInstance() {
        return instance;
    }

    /**
     * @return returns the vertx
     */
    public Vertx getVertx() {
        return this.vertx;
    }

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }
}
