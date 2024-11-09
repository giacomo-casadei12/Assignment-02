package sap.ass02.rideservice.infrastructure;

import sap.ass02.rideservice.domain.BusinessLogicL.PersistenceNotificationService;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManager;
import sap.ass02.rideservice.domain.BusinessLogicL.RideManagerImpl;
import sap.ass02.rideservice.domain.ports.AppManager;
import sap.ass02.rideservice.domain.ports.AppManagerImpl;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.RideDA;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.UserDA;
import sap.ass02.rideservice.infrastructure.DataAccessL.EBikeDB;
import sap.ass02.rideservice.infrastructure.DataAccessL.RideDB;
import sap.ass02.rideservice.infrastructure.DataAccessL.UserDB;

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
        EBikeDA bikeDA = new EBikeDB();
        UserDA userDA = new UserDB();
        AppManager am = new AppManagerImpl(r, rideDA, bikeDA, userDA);
        new WebController(am);
        r.attachPersistenceNotificationService((PersistenceNotificationService) am);
    }

}
