package sap.ass02.bikeservice;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
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
        hazelcastConfig.addListenerConfig(new ListenerConfig(new MembershipListener(){
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                String groda = membershipEvent.getMembers().iterator().next().getAttribute("MEMBER_NAME");
                System.out.println(groda);
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                System.out.println("non Groda");
            }
        }));
        ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);


        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new Verticle2(), res -> {
                    if(res.succeeded()) {
                        System.out.println("Deployment id is: " + res.result());

                    }
                });
            } else {
                System.out.println("Cluster up failed: " + cluster.cause());
            }
        });
    }
}
