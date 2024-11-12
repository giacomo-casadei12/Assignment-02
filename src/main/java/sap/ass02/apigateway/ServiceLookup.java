package sap.ass02.apigateway;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.Optional;

public interface ServiceLookup {

    void setVertxInstance(Vertx vertx);

    boolean isBikeServiceConnected();
    void plugBikeService(String host, int port);
    void unplugBikeService();
    Optional<WebClient> getBikeClient();

    boolean isUserServiceConnected();
    void plugUserService(String host, int port);
    void unplugUserService();
    Optional<WebClient> getUserClient();

    boolean isRideServiceConnected();
    void plugRideService(String host, int port);
    void unplugRideService();
    Optional<WebClient> getRideClient();
}
