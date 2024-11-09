package sap.ass02.bikeservice;

import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class BikeMain {
    public static void main(String[] args) {
        /*
        ITopic<String> topic = hazelcastInstance.getTopic("Grodus");

        // Send a message to the topic
        topic.publish("Hello from Hazelcast!");

        System.out.println("Message sent to topic.");
        */
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("my-cluster");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttribute("MEMBER_NAME","BikeService"));

        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);


        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new Verticle2(), res -> {
                    if(res.succeeded()) {
                        System.out.println("Deployment id is: " + res.result());
                        HazelcastInstance hzInstance = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();

                        hzInstance.getLifecycleService().addLifecycleListener(event -> {
                            if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_CONNECTED) {
                                // Wait for the node to be fully connected to the cluster
                                System.out.println("Cluster member is now connected!");

                                // Now get all members in the cluster
                                for (Member member : hzInstance.getCluster().getMembers()) {
                                    String memberName = member.getAttribute("MEMBER_NAME");
                                    if ("BikeService".equals(memberName)) {
                                        System.out.println("Found member with name: " + memberName);
                                    } else {
                                        System.out.println("Not found member with name: " + memberName);
                                    }
                                }
                            } else {
                                System.out.println("Cluster member is not connected!");
                            }
                        });
                    }
                });
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }
}
