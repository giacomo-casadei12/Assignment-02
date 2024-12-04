package sap.ass02.configurationserver.infrastructure;

import sap.ass02.configurationserver.domain.ConfigurationFilesObserver;

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
        ConfigurationFilesObserver cfo = new ConfigurationFilesObserver();
        new WebController(cfo);
    }

}
