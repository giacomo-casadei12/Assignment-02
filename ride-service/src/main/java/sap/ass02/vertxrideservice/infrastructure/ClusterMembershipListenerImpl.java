package sap.ass02.vertxrideservice.infrastructure;

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
            if (serviceName.equals("ApiGateway")) {
                if (!serviceLookup.isAPIGatewayServiceConnected()) {
                    serviceLookup.plugAPIGatewayService(member.getAttribute("SERVICE_ADDRESS"),
                            Integer.parseInt(member.getAttribute("SERVICE_PORT")));
                }
            }
            if (serviceName.equals("ConfigurationServer")) {
                if (!serviceLookup.isConfigurationServerConnected()) {
                    serviceLookup.plugConfigurationServer(member.getAttribute("SERVICE_ADDRESS"),
                            Integer.parseInt(member.getAttribute("SERVICE_PORT")));
                }
            }
        }
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Set<Member> members = membershipEvent.getMembers();
        for (Member member : members) {
            String serviceName = member.getAttribute("SERVICE_NAME");
            if (serviceName.equals("ApiGateway")) {
                if (serviceLookup.isAPIGatewayServiceConnected()) {
                    serviceLookup.unplugAPIGatewayService();
                }
            }
            if (serviceName.equals("ConfigurationServer")) {
                if (serviceLookup.isConfigurationServerConnected()) {
                    serviceLookup.unplugConfigurationServer();
                }
            }
        }
    }
}
