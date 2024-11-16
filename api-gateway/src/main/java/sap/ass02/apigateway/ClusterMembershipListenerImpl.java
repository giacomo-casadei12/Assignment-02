package sap.ass02.apigateway;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;

import java.util.Set;

public class ClusterMembershipListenerImpl implements ClusterMembershipListener {

    private final ServiceLookup serviceLookup;

    public ClusterMembershipListenerImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
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
                    System.out.println("pipo");
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
