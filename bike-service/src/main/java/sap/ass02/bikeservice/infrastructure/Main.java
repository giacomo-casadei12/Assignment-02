package sap.ass02.bikeservice.infrastructure;

import sap.ass02.bikeservice.domain.ports.AppManager;
import sap.ass02.bikeservice.domain.ports.AppManagerImpl;
import sap.ass02.bikeservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.bikeservice.infrastructure.DataAccessL.EBikeDB;

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
        EBikeDA bikeDA = new EBikeDB();
        AppManager am = new AppManagerImpl(bikeDA);
        new WebController(am);
    }

}
