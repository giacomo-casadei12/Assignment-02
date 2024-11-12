package sap.ass02.apigateway;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceLookupImpl implements ServiceLookup {

    private String ride_server_host = "";
    private String user_server_host = "";
    private String bike_server_host = "";
    private int ride_server_port = 0;
    private int user_server_port = 0;
    private int bike_server_port = 0;
    private Vertx vertx;
    private WebClient rideClient, userClient, bikeClient;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");

    @Override
    public void setVertxInstance(Vertx vertx) {
        this.vertx = vertx;
        if (bike_server_port != 0 && !bike_server_host.isBlank() && Objects.isNull(bikeClient)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(bike_server_host).setDefaultPort(bike_server_port);
            bikeClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "BikeService registered ");
        }
        if (user_server_port != 0 && !user_server_host.isBlank() && Objects.isNull(userClient)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(user_server_host).setDefaultPort(user_server_port);
            userClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "UserService registered ");
        }
        if (ride_server_port != 0 && !ride_server_host.isBlank() && Objects.isNull(rideClient)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(ride_server_host).setDefaultPort(ride_server_port);
            rideClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "RideService registered ");
        }
    }

    @Override
    public boolean isBikeServiceConnected() {
        return !(bike_server_host.isBlank() && bike_server_port == 0 && Objects.isNull(bikeClient));
    }

    @Override
    public void plugBikeService(String host, int port) {
        bike_server_port = port;
        bike_server_host = host;
        if (Objects.isNull(bikeClient) && !Objects.isNull(vertx)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(bike_server_host).setDefaultPort(bike_server_port);
            bikeClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "BikeService registered ");
        }
    }

    @Override
    public void unplugBikeService() {
        bike_server_port = 0;
        bike_server_host = "";
        bikeClient = null;
    }

    @Override
    public Optional<WebClient> getBikeClient() {
        return Objects.isNull(bikeClient) ? Optional.empty() : Optional.of(bikeClient);
    }

    @Override
    public boolean isUserServiceConnected() {
        return !(user_server_host.isBlank() && user_server_port == 0 && Objects.isNull(userClient));
    }

    @Override
    public void plugUserService(String host, int port) {
        user_server_port = port;
        user_server_host = host;
        if (Objects.isNull(userClient) && !Objects.isNull(vertx)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(user_server_host).setDefaultPort(user_server_port);
            userClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "UserService registered ");
        }
    }

    @Override
    public void unplugUserService() {
        user_server_port = 0;
        user_server_host = "";
        userClient = null;
    }

    @Override
    public Optional<WebClient> getUserClient() {
        return Objects.isNull(userClient) ? Optional.empty() : Optional.of(userClient);
    }

    @Override
    public boolean isRideServiceConnected() {
        return !(ride_server_host.isBlank() && ride_server_port == 0 && Objects.isNull(rideClient));
    }

    @Override
    public void plugRideService(String host, int port) {
        ride_server_host = host;
        ride_server_port = port;
        if (Objects.isNull(rideClient) && !Objects.isNull(vertx)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(ride_server_host).setDefaultPort(ride_server_port);
            rideClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "RideService registered ");
        }
    }

    @Override
    public void unplugRideService() {
        ride_server_port = 0;
        ride_server_host = "";
        rideClient = null;
    }

    @Override
    public Optional<WebClient> getRideClient() {
        return Objects.isNull(rideClient) ? Optional.empty() : Optional.of(rideClient);
    }
}
