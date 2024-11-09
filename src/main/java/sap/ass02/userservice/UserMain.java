package sap.ass02.userservice;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class UserMain {
    public static void main(String[] args) {

        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("my-cluster");
        hazelcastConfig.setMemberAttributeConfig(new MemberAttributeConfig().setAttribute("MEMBER_NAME","UserService"));
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

        /*HazelcastInstance hz = Hazelcast.newHazelcastInstance(hazelcastConfig);
        hz.getCluster().*/

        // Create VertxOptions with the Hazelcast Cluster Manager
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new Verticle1(), res -> {
                    if(res.succeeded()){
                        System.out.println("Deployment id is: " + res.result());
                        //HazelcastInstance hzInstance = ((HazelcastClusterManager) clusterManager).getHazelcastInstance();

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
/*
                        hzInstance.getLifecycleService().addLifecycleListener(event -> {
                            String groda = "groda";
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
                        });*/