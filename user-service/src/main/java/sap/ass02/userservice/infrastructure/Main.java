package sap.ass02.userservice.infrastructure;

import sap.ass02.userservice.domain.ports.AppManager;
import sap.ass02.userservice.domain.ports.AppManagerImpl;
import sap.ass02.userservice.domain.ports.ResourceNotification;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;
import sap.ass02.userservice.infrastructure.DataAccessL.UserDB;

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
        UserDA userDA = new UserDB();
        AppManager am = new AppManagerImpl(userDA);
        ResourceNotification rn = new WebController(am);
        am.attachResourceNotification(rn);
    }

}
