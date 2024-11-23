package sap.ass02.vertxrideservice;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.Optional;

public interface ServiceLookup {

    void setVertxInstance(Vertx vertx);

    boolean isAPIGatewayServiceConnected();
    void plugAPIGatewayService(String host, int port);
    void unplugAPIGatewayService();
    Optional<WebClient> getAPIGatewayClient();

    boolean isConfigurationServerConnected();
    void plugConfigurationServer(String host, int port);
    void unplugConfigurationServer();
    Optional<WebClient> getConfigurationServerClient();

}
