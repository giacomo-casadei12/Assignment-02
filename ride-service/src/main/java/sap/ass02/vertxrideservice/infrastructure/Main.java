package sap.ass02.vertxrideservice.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import sap.ass02.vertxrideservice.ClusterMembershipListenerImpl;
import sap.ass02.vertxrideservice.ServiceLookup;
import sap.ass02.vertxrideservice.ServiceLookupImpl;
import sap.ass02.vertxrideservice.domain.ports.AppManager;
import sap.ass02.vertxrideservice.infrastructure.DataAccessL.RidePersistence;
import sap.ass02.vertxrideservice.utils.VertxSingleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Main class for EBikeCesena server.
 */
public class Main {

    /**
     * The entry point of server application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        VertxSingleton vs = new VertxSingleton();
        ServiceLookup serviceLookup = new ServiceLookupImpl();
        WebController wc = new WebController(serviceLookup);
        AppManager am = new AppManager();
        RidePersistence rp = new RidePersistence();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("EBikeCesena");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SERVICE_NAME","RideService");
        attributes.put("SERVICE_ADDRESS","localhost");
        attributes.put("SERVICE_PORT","8080");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttributes(attributes));
        hazelcastConfig.addListenerConfig(new ListenerConfig(new ClusterMembershipListenerImpl(serviceLookup)));
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                vs.setVertx(cluster.result());
                serviceLookup.setVertxInstance(vs.getVertx());
                wc.attachClusterManager(clusterManager);
                vs.getVertx().deployVerticle(wc);
                vs.getVertx().deployVerticle(am);
                vs.getVertx().deployVerticle(rp);
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });

    }

}
