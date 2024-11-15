package sap.ass02.rideservice;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;

public interface ClusterMembershipListener extends MembershipListener {
    void memberAdded(MembershipEvent membershipEvent);

    void memberRemoved(MembershipEvent membershipEvent);
}
