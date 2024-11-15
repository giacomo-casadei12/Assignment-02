package sap.ass02.rideservice.infrastructure;

import sap.ass02.rideservice.domain.BusinessLogicL.NotificationService;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManager;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManagerImpl;
import sap.ass02.rideservice.domain.ports.AppManager;
import sap.ass02.rideservice.domain.ports.AppManagerImpl;
import sap.ass02.rideservice.domain.ports.ResourceRequest;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.RideDA;
import sap.ass02.rideservice.infrastructure.DataAccessL.RideDB;

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
        RideManager r = new RideManagerImpl();
        RideDA rideDA = new RideDB();
        AppManager am = new AppManagerImpl(r, rideDA);
        ResourceRequest rr = new WebController(am);
        r.attachPersistenceNotificationService((NotificationService) am);
        am.attachResourceRequest(rr);
    }

}
