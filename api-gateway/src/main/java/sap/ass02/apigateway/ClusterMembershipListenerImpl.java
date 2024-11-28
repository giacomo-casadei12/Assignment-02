package sap.ass02.apigateway;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import io.prometheus.metrics.core.metrics.Gauge;

import java.util.Set;

public class ClusterMembershipListenerImpl implements ClusterMembershipListener {

    private final ServiceLookup serviceLookup;
    private final Gauge membersGauge;

    public ClusterMembershipListenerImpl(ServiceLookup serviceLookup, Gauge membersGauge) {
        super();
        this.serviceLookup = serviceLookup;
        this.membersGauge = membersGauge;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        this.membersGauge.inc();
        Set<Member> members = membershipEvent.getMembers();
        for (Member member : members) {
            String serviceName = member.getAttribute("SERVICE_NAME");
            switch (serviceName) {
                case "BikeService":
                    if (!serviceLookup.isBikeServiceConnected()) {
                        serviceLookup.plugBikeService(member.getAttribute("SERVICE_ADDRESS"),
                                Integer.parseInt(member.getAttribute("SERVICE_PORT")));
                    }
                    break;
                case "UserService":
                    System.out.println(member.getAttribute("SERVICE_ADDRESS"));
                    if (!serviceLookup.isUserServiceConnected()) {
                        serviceLookup.plugUserService(member.getAttribute("SERVICE_ADDRESS"),
                                Integer.parseInt(member.getAttribute("SERVICE_PORT")));
                    }
                    break;
                case "RideService":
                    if (!serviceLookup.isRideServiceConnected()) {
                        serviceLookup.plugRideService(member.getAttribute("SERVICE_ADDRESS"),
                                Integer.parseInt(member.getAttribute("SERVICE_PORT")));
                    }
                    break;
            }
        }
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        this.membersGauge.dec();
        Set<Member> members = membershipEvent.getMembers();
        for (Member member : members) {
            String serviceName = member.getAttribute("SERVICE_NAME");
            switch (serviceName) {
                case "BikeService":
                    if (serviceLookup.isBikeServiceConnected()) {
                        serviceLookup.unplugBikeService();
                    }
                    break;
                case "UserService":
                    if (serviceLookup.isUserServiceConnected()) {
                        serviceLookup.unplugUserService();
                    }
                    break;
                case "RideService":
                    if (serviceLookup.isRideServiceConnected()) {
                        serviceLookup.unplugRideService();
                    }
                    break;
            }
        }
    }
}
