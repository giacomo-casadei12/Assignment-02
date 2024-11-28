package sap.ass02.vertxrideservice.infrastructure;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceLookupImpl implements ServiceLookup {

    private String api_gateway_server_host = "";
    private int api_gateway_server_port = 0;
    private Vertx vertx;
    private WebClient apiGatewayClient;
    private String configuration_server_host = "";
    private int configuration_server_port = 0;
    private WebClient configurationServerClient;
    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena]");

    @Override
    public void setVertxInstance(Vertx vertx) {
        this.vertx = vertx;
        if (api_gateway_server_port != 0 && !api_gateway_server_host.isBlank() && Objects.isNull(apiGatewayClient)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(api_gateway_server_host).setDefaultPort(api_gateway_server_port);
            apiGatewayClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "apiGatewayService registered ");
        }
        if (configuration_server_port != 0 && !configuration_server_host.isBlank() && Objects.isNull(configurationServerClient)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(configuration_server_host).setDefaultPort(configuration_server_port);
            configurationServerClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "Configuration Server registered ");
        }
    }

    @Override
    public boolean isAPIGatewayServiceConnected() {
        return !(api_gateway_server_host.isBlank() && api_gateway_server_port == 0 && Objects.isNull(apiGatewayClient));
    }

    @Override
    public void plugAPIGatewayService(String host, int port) {
        api_gateway_server_port = port;
        api_gateway_server_host = host;
        if (Objects.isNull(apiGatewayClient) && !Objects.isNull(vertx)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(api_gateway_server_host).setDefaultPort(api_gateway_server_port);
            apiGatewayClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "APIGateway registered ");
        }
    }

    @Override
    public void unplugAPIGatewayService() {
        api_gateway_server_port = 0;
        api_gateway_server_host = "";
        apiGatewayClient = null;
    }

    @Override
    public Optional<WebClient> getAPIGatewayClient() {
        return Objects.isNull(apiGatewayClient) ? Optional.empty() : Optional.of(apiGatewayClient);
    }

    @Override
    public boolean isConfigurationServerConnected() {
        return !(configuration_server_host.isBlank() && configuration_server_port == 0 && Objects.isNull(configurationServerClient));
    }

    @Override
    public void plugConfigurationServer(String host, int port) {
        configuration_server_port = port;
        configuration_server_host = host;
        if (Objects.isNull(configurationServerClient) && !Objects.isNull(vertx)) {
            WebClientOptions options = new WebClientOptions().setDefaultHost(configuration_server_host).setDefaultPort(configuration_server_port);
            configurationServerClient = WebClient.create(vertx, options);
            LOGGER.log(Level.INFO, "Configuration Server registered ");
        }
    }

    @Override
    public void unplugConfigurationServer() {
        configuration_server_port = 0;
        configuration_server_host = "";
        configurationServerClient = null;
    }

    @Override
    public Optional<WebClient> getConfigurationServerClient() {
        return Objects.isNull(configurationServerClient) ? Optional.empty() : Optional.of(configurationServerClient);
    }

}
