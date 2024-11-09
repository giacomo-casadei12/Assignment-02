package sap.ass02.userservice;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class UserMain {
    public static void main(String[] args) {

        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("my-cluster");
        // Set the member name for this Vert.x node
        hazelcastConfig.setProperty("hazelcast.member.name", "UserService");
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        /*HazelcastInstance hz = Hazelcast.newHazelcastInstance(hazelcastConfig);
        hz.getCluster().*/

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new Verticle1(), res -> {
                    if(res.succeeded()){
                        System.out.println("Deployment id is: " + res.result());
                    } else {
                        System.out.println("Deployment failed!");
                    }
                });
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }

}

/*
    ITopic<String> topic = hazelcastInstance.getTopic("Grodus");

    // Send a message to the topic
    topic.publish("Hello from Hazelcast!");

    System.out.println("Message sent to topic.");
*/
